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
import dev.leonlatsch.photok.encryption.domain.crypto.KeyGen
import dev.leonlatsch.photok.encryption.domain.crypto.LegacyGcmCryptoEngine
import dev.leonlatsch.photok.encryption.domain.models.VaultSession
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@RunWith(RobolectricTestRunner::class)
class LegacyGcmCryptoEngineTest {

    private val engine = LegacyGcmCryptoEngine()
    private val legacyEncryption = LegacyEncryption()

    @Test
    fun `encrypt and decrypt roundtrip produces identical plaintext`() {
        val session = legacyEncryption.obtainSession("test-password-123")
        val plaintext = "Hello, Photok! This is a legacy GCM encrypted file.".toByteArray()

        val ciphertext = encrypt(plaintext, session)
        val decrypted = decrypt(ciphertext, session)

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun `large payload encrypt and decrypt roundtrip`() {
        val session = legacyEncryption.obtainSession("test-password-123")
        val plaintext = ByteArray(1_500_000) { (it % 256).toByte() }

        val ciphertext = encrypt(plaintext, session)
        val decrypted = decrypt(ciphertext, session)

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun `encrypt produces non-empty ciphertext`() {
        val session = legacyEncryption.obtainSession("test-password-123")
        val plaintext = "Hello Photok".toByteArray()

        val ciphertext = encrypt(plaintext, session)

        assert(ciphertext.isNotEmpty()) { "Ciphertext must not be empty" }
    }

    @Test
    fun `ciphertext is not identical to plaintext`() {
        val session = legacyEncryption.obtainSession("test-password-123")
        val plaintext = "Hello, Photok!".toByteArray()

        val ciphertext = encrypt(plaintext, session)

        assert(!plaintext.contentEquals(ciphertext)) { "Ciphertext must differ from plaintext" }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `createEncryptStream rejects VaultSession`() {
        val vaultSession = VaultSession(KeyGen().generateVaultMasterKey())
        engine.createEncryptStream(ByteArrayOutputStream(), vaultSession)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `createDecryptStream rejects VaultSession`() {
        val vaultSession = VaultSession(KeyGen().generateVaultMasterKey())
        engine.createDecryptStream(ByteArrayInputStream(ByteArray(0)), vaultSession)
    }

    @Test
    fun `decrypting with wrong password returns garbage or null stream`() {
        val session = legacyEncryption.obtainSession("correct-password")
        val wrongSession = legacyEncryption.obtainSession("wrong-password")
        val plaintext = "sensitive content".toByteArray()

        val ciphertext = encrypt(plaintext, session)

        // GCM authentication will fail on read — stream opens but reading throws or produces garbage
        val decryptStream = engine.createDecryptStream(ByteArrayInputStream(ciphertext), wrongSession)
        // Stream may be non-null, but content will not equal plaintext
        if (decryptStream != null) {
            val result = runCatching { decryptStream.readBytes() }
            if (result.isSuccess) {
                assert(!result.getOrThrow().contentEquals(plaintext)) {
                    "Wrong password must not produce correct plaintext"
                }
            }
            // A thrown exception is also acceptable — GCM auth tag mismatch
        }
        // null stream is also acceptable
    }

    // --- helpers ---

    private fun encrypt(plaintext: ByteArray, session: dev.leonlatsch.photok.encryption.domain.models.LegacySession): ByteArray {
        val output = ByteArrayOutputStream()
        val stream = engine.createEncryptStream(output, session)!!
        stream.write(plaintext)
        stream.close()
        return output.toByteArray()
    }

    private fun decrypt(ciphertext: ByteArray, session: dev.leonlatsch.photok.encryption.domain.models.LegacySession): ByteArray {
        val stream = engine.createDecryptStream(ByteArrayInputStream(ciphertext), session)!!
        return stream.readBytes()
    }
}
