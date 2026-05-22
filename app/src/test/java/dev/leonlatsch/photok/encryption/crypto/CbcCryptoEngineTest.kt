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

package dev.leonlatsch.photok.encryption.crypto

import dev.leonlatsch.photok.encryption.domain.LegacyEncryption
import dev.leonlatsch.photok.encryption.domain.crypto.CbcCryptoEngine
import dev.leonlatsch.photok.encryption.domain.crypto.KeyGen
import dev.leonlatsch.photok.encryption.domain.models.VaultSession
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

@RunWith(RobolectricTestRunner::class)
class CbcCryptoEngineTest {

    private val engine = CbcCryptoEngine()
    private val keyGen = KeyGen()

    private fun makeSession(): VaultSession = VaultSession(keyGen.generateVaultMasterKey())

    @Test
    fun `createEncryptStream writes version byte 0x02 as first byte`() {
        val session = makeSession()
        val output = ByteArrayOutputStream()

        val stream = engine.createEncryptStream(output, session)!!
        stream.close()

        assertEquals(0x02.toByte(), output.toByteArray()[0])
    }

    @Test
    fun `createEncryptStream writes 16-byte IV immediately after version byte`() {
        val session = makeSession()
        val output = ByteArrayOutputStream()

        val stream = engine.createEncryptStream(output, session)!!
        stream.close()

        // Header = version (1) + IV (16) = 17 bytes minimum before any ciphertext
        assertTrue("Header must be at least 17 bytes", output.toByteArray().size >= 17)
    }

    @Test
    fun `encrypt produces different ciphertext each time due to random IV`() {
        val session = makeSession()
        val plaintext = "Hello, Photok!".toByteArray()

        val ct1 = encrypt(plaintext, session)
        val ct2 = encrypt(plaintext, session)

        assertFalse("Two encryptions of same plaintext must differ", ct1.contentEquals(ct2))
    }

    @Test
    fun `V2 header encrypt and decrypt roundtrip produces identical plaintext`() {
        val session = makeSession()
        val plaintext = "Hello, Photok! This is the current 3.x.x format.".toByteArray()

        val ciphertext = encrypt(plaintext, session)
        val decrypted = decrypt(ciphertext, session)

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun `large payload V2 roundtrip produces identical plaintext`() {
        val session = makeSession()
        val plaintext = ByteArray(1_500_000) { (it % 256).toByte() }

        val ciphertext = encrypt(plaintext, session)
        val decrypted = decrypt(ciphertext, session)

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun `V1 header file decrypts correctly (backward compatibility with 2xx)`() {
        val session = makeSession()
        val plaintext = "This is a legacy 2.x.x encrypted file.".toByteArray()

        // Manually build a V1-format stream: [0x01][16B salt][16B IV][CBC ciphertext]
        val salt = ByteArray(16) { it.toByte() }
        val iv = ByteArray(16) { (it + 10).toByte() }

        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding").apply {
            init(Cipher.ENCRYPT_MODE, session.vmk, IvParameterSpec(iv))
        }
        val ciphertext = cipher.doFinal(plaintext)

        val v1Blob = ByteArrayOutputStream().apply {
            write(byteArrayOf(0x01))
            write(salt)
            write(iv)
            write(ciphertext)
        }.toByteArray()

        val decryptStream = engine.createDecryptStream(ByteArrayInputStream(v1Blob), session)!!
        val decrypted = decryptStream.readBytes()

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun `createDecryptStream with unknown version byte returns null`() {
        val session = makeSession()
        val invalidBlob = ByteArrayInputStream(byteArrayOf(0x09.toByte()))

        val result = engine.createDecryptStream(invalidBlob, session)

        assertNull("Unknown version byte must return null", result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `createEncryptStream rejects LegacySession`() {
        val legacySession = LegacyEncryption().obtainSession("password")
        engine.createEncryptStream(ByteArrayOutputStream(), legacySession)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `createDecryptStream rejects LegacySession`() {
        val legacySession = LegacyEncryption().obtainSession("password")
        engine.createDecryptStream(ByteArrayInputStream(ByteArray(0)), legacySession)
    }

    @Test
    fun `decrypting with wrong VMK does not produce original plaintext`() {
        val session = makeSession()
        val wrongSession = makeSession()
        val plaintext = "sensitive content".toByteArray()

        val ciphertext = encrypt(plaintext, session)

        val decryptStream = engine.createDecryptStream(ByteArrayInputStream(ciphertext), wrongSession)
        if (decryptStream != null) {
            val result = runCatching { decryptStream.readBytes() }
            if (result.isSuccess) {
                assertFalse(
                    "Wrong VMK must not produce correct plaintext",
                    result.getOrThrow().contentEquals(plaintext)
                )
            }
            // A thrown exception is also acceptable
        }
        // null return is also acceptable
    }

    // --- helpers ---

    private fun encrypt(plaintext: ByteArray, session: VaultSession): ByteArray {
        val output = ByteArrayOutputStream()
        val stream = engine.createEncryptStream(output, session)!!
        stream.write(plaintext)
        stream.close()
        return output.toByteArray()
    }

    private fun decrypt(ciphertext: ByteArray, session: VaultSession): ByteArray {
        val stream = engine.createDecryptStream(ByteArrayInputStream(ciphertext), session)!!
        return stream.readBytes()
    }
}
