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

import dev.leonlatsch.photok.encryption.domain.crypto.KeyGen
import dev.leonlatsch.photok.encryption.domain.models.Kdf
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class KeyGenTest {

    private val keyGen = KeyGen()

    @Test
    fun `generateVaultMasterKey returns 256-bit AES key`() {
        val key = keyGen.generateVaultMasterKey()

        assertEquals("AES", key.algorithm)
        assertEquals(32, key.encoded.size) // 256 bits = 32 bytes
    }

    @Test
    fun `generateVaultMasterKey returns unique keys on each call`() {
        val key1 = keyGen.generateVaultMasterKey()
        val key2 = keyGen.generateVaultMasterKey()

        assertFalse(key1.encoded.contentEquals(key2.encoded))
    }

    @Test
    fun `derivePasswordKeyEncryptionKey is deterministic for same inputs`() {
        val password = "test-password"
        val salt = ByteArray(16) { it.toByte() }

        val key1 = keyGen.derivePasswordKeyEncryptionKey(password, salt, Kdf.PBKDF2WithHmacSHA256, 1_000, 256)
        val key2 = keyGen.derivePasswordKeyEncryptionKey(password, salt, Kdf.PBKDF2WithHmacSHA256, 1_000, 256)

        assertArrayEquals(key1.encoded, key2.encoded)
    }

    @Test
    fun `derivePasswordKeyEncryptionKey produces different keys for different salts`() {
        val password = "test-password"
        val salt1 = ByteArray(16) { it.toByte() }
        val salt2 = ByteArray(16) { (it + 1).toByte() }

        val key1 = keyGen.derivePasswordKeyEncryptionKey(password, salt1, Kdf.PBKDF2WithHmacSHA256, 1_000, 256)
        val key2 = keyGen.derivePasswordKeyEncryptionKey(password, salt2, Kdf.PBKDF2WithHmacSHA256, 1_000, 256)

        assertFalse(key1.encoded.contentEquals(key2.encoded))
    }

    @Test
    fun `derivePasswordKeyEncryptionKey produces different keys for different passwords`() {
        val salt = ByteArray(16) { it.toByte() }

        val key1 = keyGen.derivePasswordKeyEncryptionKey("password-one", salt, Kdf.PBKDF2WithHmacSHA256, 1_000, 256)
        val key2 = keyGen.derivePasswordKeyEncryptionKey("password-two", salt, Kdf.PBKDF2WithHmacSHA256, 1_000, 256)

        assertFalse(key1.encoded.contentEquals(key2.encoded))
    }

    @Test
    fun `derivePasswordKeyEncryptionKey returns 256-bit key for keySize 256`() {
        val key = keyGen.derivePasswordKeyEncryptionKey(
            password = "test-password",
            salt = ByteArray(16) { it.toByte() },
            kdf = Kdf.PBKDF2WithHmacSHA256,
            kdfIterations = 1_000,
            keySize = 256,
        )

        assertEquals("AES", key.algorithm)
        assertEquals(32, key.encoded.size) // 256 bits = 32 bytes
    }
}
