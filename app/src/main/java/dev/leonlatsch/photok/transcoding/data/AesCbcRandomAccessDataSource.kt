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
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.IvParameterSpec

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

        return dataSpec.length
    }

    @Throws(IOException::class)
    override fun read(target: ByteArray, offset: Int, length: Int): Int =
        if (length == 0) 0 else inputStream?.read(target, offset, length) ?: 0

    override fun addTransferListener(transferListener: TransferListener) {}

    override fun getUri(): Uri = uri

    override fun close() {
        inputStream?.close()
        fileInputStream?.close()
    }
}