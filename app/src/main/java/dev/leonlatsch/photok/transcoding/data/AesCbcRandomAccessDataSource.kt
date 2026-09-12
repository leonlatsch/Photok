/*
 *   Copyright 2020-2026 Leon Latsch
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package dev.leonlatsch.photok.transcoding.data

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.encryption.domain.crypto.BLOCK_SIZE
import dev.leonlatsch.photok.encryption.domain.crypto.IV_SIZE
import dev.leonlatsch.photok.encryption.domain.crypto.SALT_SIZE
import dev.leonlatsch.photok.encryption.domain.models.Algorithm
import dev.leonlatsch.photok.encryption.domain.models.EncryptionVersionByte
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.IvParameterSpec

private const val AES_CBC_NO_PADDING = "AES/CBC/NoPadding"

/**
 * AES-CBC Random Access DataSource (Block-Aligned Seeking)
 *
 *  ┌────────────────────────────────────────────────────────┐
 *  │                 Encrypted File Layout                  │
 *  ├────────────────────────────────────────────────────────┤
 *  │  H  │  C0  │  C1  │  C2  │  C3  │  C4  │  ...          │
 *  └────────────────────────────────────────────────────────┘
 *   H  = [ENC_VERSION_BYTE][SALT][IV]
 *   Cn = Ciphertext block n (16 bytes each)
 *
 *  Example: Seek into plaintext that belongs to block P3.
 *  CBC requires the previous ciphertext block as IV.
 *
 *  ┌────────────────────────────────────────────────────────┐
 *  │  H  │  C0  │  C1  │  C2  │  C3  │  C4  │  ...          │
 *  └────────────────────────────────────────────────────────┘
 *                      ▲      ▲
 *                      │      │
 *                      │      └─ Read C3 (target ciphertext block)
 *                      └──────── Read C2 (used as IV for C3)
 *
 *  Steps (block-aware seek):
 *    1) Skip H and jump to C2.
 *    2) Read C2 → use as IV.
 *    3) Read C3 → decrypt with IV = C2 → produces plaintext block P3.
 *    4) Dismiss the first "discard" bytes inside P3
 *       (when the target byte is not aligned to the block boundary).
 *
 *  Discard illustration (inside P3):
 *  ┌──────────────────────────────┐
 *  │  P3: [xxxx|.............]    │
 *  └──────────────────────────────┘
 *             ↑
 *             └─ dismissed bytes (discard)
 *
 *  This avoids fake-skipping bytes by decrypting and allows correct
 *  block-aligned random access in AES-CBC.
 *
 *  Limitations:
 *  - Random access is only safe at 16-byte block boundaries.
 *  - PKCS7 padding is only validated at end-of-stream.
 */
@UnstableApi
class AesCbcRandomAccessDataSource(
    private val sessionRepository: SessionRepository,
) : DataSource {

    private var inputStream: CipherInputStream? = null
    private var fileInputStream: FileInputStream? = null
    private var bytesRemaining: Long = 0
    private lateinit var uri: Uri

    override fun open(dataSpec: DataSpec): Long {
        uri = dataSpec.uri
        uri.path ?: return 0

        val file = File(uri.path!!).canonicalFile
        val fis = FileInputStream(file)
        fileInputStream = fis
        val channel = fis.channel

        // --- Read header ---
        val versionBuf = ByteBuffer.allocate(1)
        channel.position(0)
        channel.read(versionBuf)
        versionBuf.flip()

        val version = EncryptionVersionByte.fromValue(versionBuf.get())

        channel.position(0)
        val headerBuf = ByteBuffer.allocate(version.headerSize)
        channel.read(headerBuf)
        headerBuf.flip()

        // Read/skip version byte since we have the whole header
        headerBuf.get()

        val fileIv = ByteArray(IV_SIZE)

        when (version) {
            EncryptionVersionByte.One -> {
                val salt = ByteArray(SALT_SIZE)
                headerBuf.get(salt)

                headerBuf.get(fileIv)
            }
            EncryptionVersionByte.Two -> {
                headerBuf.get(fileIv)
            }
        }

        // --- Resolve key  ---
        val key = sessionRepository.require().vmk

        // --- Determine plaintext length ---
        // See resolvePlainLength for why open() must not return C.LENGTH_UNSET.
        val plainLength = resolvePlainLength(
            channel = channel,
            version = version,
            fileIv = fileIv,
            fileLength = file.length(),
            key = key,
        )

        // --- Compute target block ---
        val plainOffset = dataSpec.position
        val blockIndex = (plainOffset / BLOCK_SIZE).toInt()
        val discard = (plainOffset % BLOCK_SIZE).toInt()

        // --- Resolve IV for target block ---
        val ivForTarget = if (blockIndex == 0) {
            fileIv
        } else {
            val prevCipherOffset = version.headerSize + (blockIndex - 1L) * BLOCK_SIZE
            channel.position(prevCipherOffset)
            val prevCipher = ByteArray(BLOCK_SIZE)
            channel.read(ByteBuffer.wrap(prevCipher))
            prevCipher
        }

        // --- Position channel at the target ciphertext block ---
        val targetCipherOffset = version.headerSize + blockIndex.toLong() * BLOCK_SIZE
        channel.position(targetCipherOffset)

        // --- Create cipher stream from this point ---
        val cipher = Cipher.getInstance(Algorithm.AesCbcPkcs7Padding.value)
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(ivForTarget))

        inputStream = CipherInputStream(Channels.newInputStream(channel), cipher)

        // --- Discard bytes inside the first decrypted block ---
        if (discard > 0) {
            val skip = ByteArray(discard)
            inputStream?.read(skip, 0, discard)
        }

        bytesRemaining = if (dataSpec.length != C.LENGTH_UNSET.toLong()) {
            dataSpec.length
        } else {
            (plainLength - plainOffset).coerceAtLeast(0)
        }

        return bytesRemaining
    }

    /**
     * Returns the plaintext length of the encrypted file.
     *
     * Media3 needs it: an mp4 box with a size field of 0 extends to the end of the file, and
     * Mp4Extractor resolves that only via the input length, which is the value open() returns.
     * With C.LENGTH_UNSET it throws "Atom size less than header length (unsupported)". Signal
     * on Android writes the mdat box that way, Signal on iOS writes an explicit size field.
     *
     * The file length is not the plaintext length: the header comes first, and the PKCS7
     * padded ciphertext is 1 to 16 bytes longer than the plaintext. The padding count is not
     * stored anywhere, but in CBC a block depends only on the block before it, so decrypting
     * the last ciphertext block with the one before it as IV is enough. NoPadding is used on
     * purpose, PKCS7Padding would strip the padding and discard the count. In PKCS7 every
     * padding byte holds the padding count, so the last decrypted byte is the padding count.
     */
    private fun resolvePlainLength(
        channel: FileChannel,
        version: EncryptionVersionByte,
        fileIv: ByteArray,
        fileLength: Long,
        key: Key,
    ): Long {
        val cipherLength = fileLength - version.headerSize

        if (cipherLength < BLOCK_SIZE || cipherLength % BLOCK_SIZE != 0L) {
            // Not a well formed CBC stream, best effort.
            return cipherLength.coerceAtLeast(0)
        }

        val lastBlockOffset = version.headerSize + cipherLength - BLOCK_SIZE

        val ivForLastBlock = if (cipherLength == BLOCK_SIZE.toLong()) {
            fileIv
        } else {
            val prev = ByteArray(BLOCK_SIZE)
            channel.position(lastBlockOffset - BLOCK_SIZE)
            channel.read(ByteBuffer.wrap(prev))
            prev
        }

        val lastBlock = ByteArray(BLOCK_SIZE)
        channel.position(lastBlockOffset)
        channel.read(ByteBuffer.wrap(lastBlock))

        val cipher = Cipher.getInstance(AES_CBC_NO_PADDING)
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(ivForLastBlock))
        val decrypted = cipher.doFinal(lastBlock)

        val padding = decrypted.last().toInt() and 0xFF
        if (padding !in 1..BLOCK_SIZE) {
            return cipherLength
        }

        return cipherLength - padding
    }

    @Throws(IOException::class)
    override fun read(target: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) return 0
        if (bytesRemaining == 0L) return C.RESULT_END_OF_INPUT

        val toRead = minOf(length.toLong(), bytesRemaining).toInt()
        val read = inputStream?.read(target, offset, toRead) ?: C.RESULT_END_OF_INPUT

        if (read == -1) {
            bytesRemaining = 0
            return C.RESULT_END_OF_INPUT
        }

        bytesRemaining -= read
        return read
    }

    override fun addTransferListener(transferListener: TransferListener) {}

    override fun getUri(): Uri = uri

    override fun close() {
        bytesRemaining = 0
        inputStream?.close()
        fileInputStream?.close()
    }
}