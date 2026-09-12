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
import androidx.media3.datasource.DataSpec
import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.encryption.domain.crypto.CbcCryptoEngine
import dev.leonlatsch.photok.encryption.domain.crypto.IV_SIZE
import dev.leonlatsch.photok.encryption.domain.crypto.KeyGen
import dev.leonlatsch.photok.encryption.domain.crypto.SALT_SIZE
import dev.leonlatsch.photok.encryption.domain.models.Algorithm
import dev.leonlatsch.photok.encryption.domain.models.EncryptionVersionByte
import dev.leonlatsch.photok.encryption.domain.models.VaultSession
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

/**
 * Unit tests for [AesCbcRandomAccessDataSource].
 *
 * Covers the plaintext length arithmetic (PKCS7 padding is not stored anywhere, so the
 * length has to be recovered from the last ciphertext block), block aligned and unaligned
 * positioned opens, end of input signalling, bounded reads and both header versions.
 *
 * These tests cannot catch the Signal mp4 regression on their own - returning
 * [C.LENGTH_UNSET] is legal per the Media3 DataSource contract. See
 * [dev.leonlatsch.photok.transcoding.integration.EncryptedVideoExtractionTest] for that.
 */
@RunWith(RobolectricTestRunner::class)
class AesCbcRandomAccessDataSourceTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val session = VaultSession(KeyGen().generateVaultMasterKey())
    private val cryptoEngine = CbcCryptoEngine()

    private val sessionRepository = mockk<SessionRepository> {
        every { require() } returns session
    }

    /**
     * PKCS7 always pads, and pads a whole block when the plaintext is an exact multiple of
     * the block size. The range covers both sides of the 16 and 32 byte boundaries.
     */
    @Test
    fun `open reports the exact plaintext length for every size around the block boundaries`() {
        for (size in 0..40) {
            val plaintext = ByteArray(size) { (it % 256).toByte() }
            val file = writeEncryptedV2(plaintext, "size-$size.enc")

            val dataSource = AesCbcRandomAccessDataSource(sessionRepository)
            val reportedLength = dataSource.open(DataSpec(Uri.fromFile(file)))
            val content = dataSource.readToEndOfInput()
            dataSource.close()

            assertEquals("Reported length for plaintext of $size bytes", size.toLong(), reportedLength)
            assertArrayEquals("Content for plaintext of $size bytes", plaintext, content)
        }
    }

    /**
     * CBC only allows random access at block boundaries, so unaligned positions are reached
     * by decrypting the containing block and discarding its leading bytes.
     */
    @Test
    fun `open at a plaintext position returns the remaining bytes from that position`() {
        val plaintext = ByteArray(1000) { (it * 31 % 251).toByte() }
        val file = writeEncryptedV2(plaintext, "positioned.enc")

        for (position in listOf(0L, 1L, 15L, 16L, 17L, 31L, 512L, 999L)) {
            val dataSource = AesCbcRandomAccessDataSource(sessionRepository)
            val reportedLength = dataSource.open(dataSpecAt(file, position))
            val content = dataSource.readToEndOfInput()
            dataSource.close()

            assertEquals("Reported length at position $position", plaintext.size - position, reportedLength)
            assertArrayEquals(
                "Content at position $position",
                plaintext.copyOfRange(position.toInt(), plaintext.size),
                content,
            )
        }
    }

    @Test
    fun `read returns end of input once the plaintext is consumed`() {
        val plaintext = ByteArray(64) { it.toByte() }
        val file = writeEncryptedV2(plaintext, "end-of-input.enc")

        val dataSource = AesCbcRandomAccessDataSource(sessionRepository)
        dataSource.open(DataSpec(Uri.fromFile(file)))

        val buffer = ByteArray(plaintext.size)
        var total = 0
        while (total < plaintext.size) {
            val read = dataSource.read(buffer, total, plaintext.size - total)
            assertNotEquals("Unexpected end of input after $total bytes", C.RESULT_END_OF_INPUT, read)
            total += read
        }

        assertArrayEquals(plaintext, buffer)
        assertEquals(C.RESULT_END_OF_INPUT, dataSource.read(buffer, 0, buffer.size))
        assertEquals(C.RESULT_END_OF_INPUT, dataSource.read(buffer, 0, buffer.size))

        dataSource.close()
    }

    @Test
    fun `a bounded DataSpec length caps the reported length and the delivered bytes`() {
        val plaintext = ByteArray(1000) { (it * 31 % 251).toByte() }
        val file = writeEncryptedV2(plaintext, "bounded.enc")

        val dataSource = AesCbcRandomAccessDataSource(sessionRepository)
        val reportedLength = dataSource.open(
            DataSpec.Builder()
                .setUri(Uri.fromFile(file))
                .setPosition(100)
                .setLength(50)
                .build()
        )
        val content = dataSource.readToEndOfInput()
        dataSource.close()

        assertEquals(50L, reportedLength)
        assertArrayEquals(plaintext.copyOfRange(100, 150), content)
    }

    /**
     * Media3 reopens the same DataSource instance when the extractor seeks, so a closed
     * source has to be usable again.
     */
    @Test
    fun `a closed data source can be opened again`() {
        val plaintext = ByteArray(1000) { (it * 31 % 251).toByte() }
        val file = writeEncryptedV2(plaintext, "reuse.enc")
        val dataSource = AesCbcRandomAccessDataSource(sessionRepository)

        val firstLength = dataSource.open(DataSpec(Uri.fromFile(file)))
        val firstContent = dataSource.readToEndOfInput()
        dataSource.close()

        val secondLength = dataSource.open(dataSpecAt(file, 500))
        val secondContent = dataSource.readToEndOfInput()
        dataSource.close()

        assertEquals(1000L, firstLength)
        assertArrayEquals(plaintext, firstContent)
        assertEquals(500L, secondLength)
        assertArrayEquals(plaintext.copyOfRange(500, 1000), secondContent)
    }

    /**
     * V1 files (written by 2.x.x) carry an extra salt in the header, so every ciphertext
     * offset shifts by [SALT_SIZE].
     */
    @Test
    fun `V1 header files are read like V2 header files`() {
        val plaintext = ByteArray(300) { (it * 7 % 251).toByte() }
        val v1File = writeEncryptedV1(plaintext, "header-v1.enc")
        val v2File = writeEncryptedV2(plaintext, "header-v2.enc")

        for (file in listOf(v1File, v2File)) {
            val dataSource = AesCbcRandomAccessDataSource(sessionRepository)

            val fullLength = dataSource.open(DataSpec(Uri.fromFile(file)))
            val fullContent = dataSource.readToEndOfInput()
            dataSource.close()

            val positionedLength = dataSource.open(dataSpecAt(file, 137))
            val positionedContent = dataSource.readToEndOfInput()
            dataSource.close()

            assertEquals("Full length of ${file.name}", 300L, fullLength)
            assertArrayEquals("Full content of ${file.name}", plaintext, fullContent)
            assertEquals("Positioned length of ${file.name}", 163L, positionedLength)
            assertArrayEquals(
                "Positioned content of ${file.name}",
                plaintext.copyOfRange(137, 300),
                positionedContent,
            )
        }
    }

    private fun dataSpecAt(file: File, position: Long) = DataSpec.Builder()
        .setUri(Uri.fromFile(file))
        .setPosition(position)
        .build()

    private fun AesCbcRandomAccessDataSource.readToEndOfInput(): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(256)

        while (true) {
            val read = read(buffer, 0, buffer.size)
            if (read == C.RESULT_END_OF_INPUT) break
            output.write(buffer, 0, read)
        }

        return output.toByteArray()
    }

    /** Writes `[0x02][IV][ENCRYPTED_DATA]` using the production encrypt stream. */
    private fun writeEncryptedV2(plaintext: ByteArray, name: String): File {
        val file = tempFolder.newFile(name)

        FileOutputStream(file).use { fileOutput ->
            cryptoEngine.createEncryptStream(fileOutput, session)!!.use { it.write(plaintext) }
        }

        return file
    }

    /** Writes `[0x01][SALT][IV][ENCRYPTED_DATA]`, the header the 2.x.x app produced. */
    private fun writeEncryptedV1(plaintext: ByteArray, name: String): File {
        val file = tempFolder.newFile(name)
        val salt = ByteArray(SALT_SIZE) { (it + 100).toByte() }
        val iv = ByteArray(IV_SIZE) { (it + 1).toByte() }

        val cipher = Cipher.getInstance(Algorithm.AesCbcPkcs7Padding.value).apply {
            init(Cipher.ENCRYPT_MODE, session.vmk, IvParameterSpec(iv))
        }

        FileOutputStream(file).use { fileOutput ->
            fileOutput.write(byteArrayOf(EncryptionVersionByte.One.value))
            fileOutput.write(salt)
            fileOutput.write(iv)
            fileOutput.write(cipher.doFinal(plaintext))
        }

        return file
    }
}
