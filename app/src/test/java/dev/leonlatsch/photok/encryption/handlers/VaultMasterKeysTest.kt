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

package dev.leonlatsch.photok.encryption.handlers

import dev.leonlatsch.photok.encryption.domain.crypto.KeyGen
import dev.leonlatsch.photok.encryption.domain.crypto.VAULT_MASTER_KEY_SIZE
import dev.leonlatsch.photok.encryption.domain.handlers.PasswordVaultProtectionHandler
import dev.leonlatsch.photok.encryption.domain.handlers.toVaultMasterKey
import dev.leonlatsch.photok.encryption.domain.models.CreateRequest
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import kotlin.io.encoding.Base64
import dev.leonlatsch.photok.settings.data.Config as AppConfig

@RunWith(RobolectricTestRunner::class)
class VaultMasterKeysTest {

    private val keyGen = KeyGen()
    private val mockConfig = mockk<AppConfig>(relaxed = true).apply {
        every { legacyPasswordHash } returns null
        every { legacyUserSalt } returns null
    }
    private val handler = PasswordVaultProtectionHandler(keyGen, mockConfig)

    private val password = "correct-horse-battery-staple"

    @Test
    fun `32 bytes become an AES key with the same bytes`() {
        val bytes = ByteArray(VAULT_MASTER_KEY_SIZE) { it.toByte() }

        val key = toVaultMasterKey(bytes)

        assertEquals("AES", key.algorithm)
        assertArrayEquals(bytes, key.encoded)
    }

    @Test
    fun `any other length is rejected`() {
        listOf(0, 1, 16, 24, 31, 33, 48).forEach { size ->
            val result = runCatching { toVaultMasterKey(ByteArray(size)) }

            assertTrue(
                "A $size byte key must be rejected",
                result.exceptionOrNull() is IllegalArgumentException,
            )
        }
    }

    /**
     * Simulates a wrong key that happens to produce valid padding: the wrapped VMK decrypts
     * cleanly, but to 16 bytes instead of 32. Unlock must fail instead of returning that key.
     */
    @Test
    fun `unlock fails when the unwrapped key has the wrong length`() = runTest {
        val protection = handler.create(CreateRequest.Password(password))
        val params = protection.params

        val kek = keyGen.derivePasswordKeyEncryptionKey(
            password = password,
            salt = Base64.decode(requireNotNull(params.salt)),
            kdf = requireNotNull(params.kdf),
            kdfIterations = requireNotNull(params.kdfIterations),
            keySize = params.keySize,
        )
        val cipher = Cipher.getInstance(params.algorithm.value).apply {
            init(Cipher.ENCRYPT_MODE, kek, IvParameterSpec(Base64.decode(params.iv)))
        }
        val shortProtection = protection.copy(wrappedVMK = cipher.doFinal(ByteArray(16)))

        val result = runCatching {
            handler.unlock(UnlockRequest.Password(password), shortProtection)
        }

        assertTrue(
            "A 16 byte VMK must not unlock the vault",
            result.exceptionOrNull() is IllegalArgumentException,
        )
    }
}
