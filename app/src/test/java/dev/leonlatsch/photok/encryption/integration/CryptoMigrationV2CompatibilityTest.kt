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

package dev.leonlatsch.photok.encryption.integration

import dev.leonlatsch.photok.encryption.domain.crypto.CbcCryptoEngine
import dev.leonlatsch.photok.encryption.domain.crypto.KeyGen
import dev.leonlatsch.photok.encryption.domain.models.Algorithm
import dev.leonlatsch.photok.encryption.domain.models.Kdf
import dev.leonlatsch.photok.encryption.domain.models.VaultSession
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

/**
 * Integration test verifying that the 3.x.x [CbcCryptoEngine] can still read
 * files encrypted in the 2.x.x format (header version 0x01: version + salt + IV + ciphertext).
 *
 * This is the backward-compatibility guarantee: 2.x.x files must always be decryptable
 * by the current engine without any re-encryption step.
 */
@RunWith(RobolectricTestRunner::class)
class CryptoMigrationV2CompatibilityTest {

    private val cbcEngine = CbcCryptoEngine()
    private val keyGen = KeyGen()

    /**
     * Simulates a 2.x.x encrypted file:
     * - Key derived via PBKDF2 from password + salt (the VMK for 2.x.x is this derived key)
     * - File header: [0x01][salt (16B)][IV (16B)][ciphertext]
     */
    @Test
    fun `CbcCryptoEngine decrypts a V1-header file produced by the 2xx format`() {
        val password = "legacy-2xx-password"
        val salt = ByteArray(16) { (it * 7).toByte() }
        val iv = ByteArray(16) { (it * 3 + 1).toByte() }
        val plaintext = "This is a 2.x.x encrypted photo.".toByteArray()

        // Derive the 2.x.x user key (this is the VMK for 3.x.x after 2→3 migration)
        val vmk = keyGen.derivePasswordKeyEncryptionKey(
            password = password,
            salt = salt,
            kdf = Kdf.PBKDF2WithHmacSHA256,
            kdfIterations = 100_000,
            keySize = 256,
        )

        // Build a V1-header blob manually, just like 2.x.x wrote it
        val ciphertext = Cipher.getInstance(Algorithm.AesCbcPkcs7Padding.value).run {
            init(Cipher.ENCRYPT_MODE, vmk, IvParameterSpec(iv))
            doFinal(plaintext)
        }

        val v1Blob = ByteArrayOutputStream().apply {
            write(byteArrayOf(0x01))
            write(salt)
            write(iv)
            write(ciphertext)
        }.toByteArray()

        // Decrypt via the 3.x.x engine using the same VMK
        val vaultSession = VaultSession(vmk)
        val decryptStream = cbcEngine.createDecryptStream(ByteArrayInputStream(v1Blob), vaultSession)!!
        val decrypted = decryptStream.readBytes()

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun `CbcCryptoEngine decrypts a large V1-header file`() {
        val salt = ByteArray(16) { it.toByte() }
        val iv = ByteArray(16) { (it + 5).toByte() }
        val plaintext = ByteArray(1_500_000) { (it % 256).toByte() }

        val vmk = keyGen.generateVaultMasterKey()

        val ciphertext = Cipher.getInstance(Algorithm.AesCbcPkcs7Padding.value).run {
            init(Cipher.ENCRYPT_MODE, vmk, IvParameterSpec(iv))
            doFinal(plaintext)
        }

        val v1Blob = ByteArrayOutputStream().apply {
            write(byteArrayOf(0x01))
            write(salt)
            write(iv)
            write(ciphertext)
        }.toByteArray()

        val vaultSession = VaultSession(vmk)
        val decryptStream = cbcEngine.createDecryptStream(ByteArrayInputStream(v1Blob), vaultSession)!!
        val decrypted = decryptStream.readBytes()

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun `V2-header files (3xx format) are still decryptable after reading a V1-header file`() {
        // Verifies the engine handles both header versions without leaking state
        val vmk = keyGen.generateVaultMasterKey()
        val vaultSession = VaultSession(vmk)
        val plaintext = "3.x.x format content".toByteArray()

        // Encrypt as V2
        val v2Output = ByteArrayOutputStream()
        val v2Stream = cbcEngine.createEncryptStream(v2Output, vaultSession)!!
        v2Stream.write(plaintext)
        v2Stream.close()

        // Also create a V1 blob for the same VMK
        val iv = ByteArray(16) { it.toByte() }
        val v1Ciphertext = Cipher.getInstance(Algorithm.AesCbcPkcs7Padding.value).run {
            init(Cipher.ENCRYPT_MODE, vmk, IvParameterSpec(iv))
            doFinal(plaintext)
        }
        val v1Blob = ByteArrayOutputStream().apply {
            write(byteArrayOf(0x01))
            write(ByteArray(16)) // salt (not used for decryption)
            write(iv)
            write(v1Ciphertext)
        }.toByteArray()

        // Decrypt V1
        val v1Decrypted = cbcEngine.createDecryptStream(ByteArrayInputStream(v1Blob), vaultSession)!!.readBytes()

        // Decrypt V2
        val v2Decrypted = cbcEngine.createDecryptStream(ByteArrayInputStream(v2Output.toByteArray()), vaultSession)!!.readBytes()

        assertArrayEquals(plaintext, v1Decrypted)
        assertArrayEquals(plaintext, v2Decrypted)
    }
}
