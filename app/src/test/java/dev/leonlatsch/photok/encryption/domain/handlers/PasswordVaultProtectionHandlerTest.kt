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

package dev.leonlatsch.photok.encryption.domain.handlers

import dev.leonlatsch.photok.encryption.domain.crypto.KeyGen
import dev.leonlatsch.photok.encryption.domain.models.CreateRequest
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionType
import dev.leonlatsch.photok.settings.data.Config as AppConfig
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mindrot.jbcrypt.BCrypt
import org.robolectric.RobolectricTestRunner
import java.util.Base64

@RunWith(RobolectricTestRunner::class)
class PasswordVaultProtectionHandlerTest {

    private val keyGen = KeyGen()
    private val mockConfig = mockk<AppConfig>(relaxed = true)
    private val handler = PasswordVaultProtectionHandler(keyGen, mockConfig)

    private val testPassword = "correct-horse-battery-staple"

    // Computed once: low bcrypt cost for test speed
    private val testBcryptHash = BCrypt.hashpw(testPassword, BCrypt.gensalt(4))

    // A base64-encoded 16-byte salt simulating a 2.x.x legacy user salt
    private val legacySalt = ByteArray(16) { (it * 3).toByte() }
    private val legacySaltBase64 = Base64.getEncoder().encodeToString(legacySalt)

    @Before
    fun setup() {
        every { mockConfig.legacyPasswordHash } returns null
        every { mockConfig.legacyUserSalt } returns null
    }

    // --- create ---

    @Test
    fun `create produces VaultProtection with all required fields`() = runTest {
        val protection = handler.create(CreateRequest.Password(testPassword))

        assertEquals(VaultProtectionType.Password, protection.type)
        assertNotNull(protection.params.salt)
        assertNotNull(protection.params.iv)
        assertNotNull(protection.params.kdf)
        assertNotNull(protection.params.kdfIterations)
        assertTrue(protection.wrappedVMK.isNotEmpty())
    }

    @Test
    fun `create produces different salt and IV on each call`() = runTest {
        val p1 = handler.create(CreateRequest.Password(testPassword))
        val p2 = handler.create(CreateRequest.Password(testPassword))

        assertFalse(
            "Two create calls must produce different salts",
            p1.params.salt == p2.params.salt
        )
        assertFalse(
            "Two create calls must produce different IVs",
            p1.params.iv == p2.params.iv
        )
    }

    // --- unlock ---

    @Test
    fun `create then unlock returns same VMK bytes`() = runTest {
        val protection = handler.create(CreateRequest.Password(testPassword))
        val request = UnlockRequest.Password(testPassword)

        val vmk = handler.unlock(request, protection)

        // The VMK encoded bytes must be 32 bytes (256-bit AES key)
        assertEquals(32, vmk.encoded.size)
        assertEquals("AES", vmk.algorithm)

        // Re-unlock to confirm determinism
        val vmk2 = handler.unlock(request, protection)
        assertArrayEquals(vmk.encoded, vmk2.encoded)
    }

    @Test(expected = Exception::class)
    fun `unlock with wrong password throws`() = runTest {
        val protection = handler.create(CreateRequest.Password(testPassword))
        handler.unlock(UnlockRequest.Password("wrong-password"), protection)
    }

    // --- canMigrate ---

    @Test
    fun `canMigrate returns true when both legacyPasswordHash and legacyUserSalt are present`() = runTest {
        every { mockConfig.legacyPasswordHash } returns testBcryptHash
        every { mockConfig.legacyUserSalt } returns legacySaltBase64

        assertTrue(handler.canMigrate())
    }

    @Test
    fun `canMigrate returns false when legacyPasswordHash is missing`() = runTest {
        every { mockConfig.legacyPasswordHash } returns null
        every { mockConfig.legacyUserSalt } returns legacySaltBase64

        assertFalse(handler.canMigrate())
    }

    @Test
    fun `canMigrate returns false when legacyUserSalt is missing`() = runTest {
        every { mockConfig.legacyPasswordHash } returns testBcryptHash
        every { mockConfig.legacyUserSalt } returns null

        // TODO @leonlatsch: This returns false for 1.x.x users (no legacyUserSalt) even though
        //  migrate() handles that case. Revisit whether 1.x.x users can migrate via password.
        assertFalse(handler.canMigrate())
    }

    @Test
    fun `canMigrate returns false when both legacy fields are missing`() = runTest {
        assertFalse(handler.canMigrate())
    }

    // --- migrate from 1.x.x (no legacy salt) ---

    @Test
    fun `migrate from 1xx generates a fresh random VMK`() = runTest {
        every { mockConfig.legacyPasswordHash } returns testBcryptHash
        every { mockConfig.legacyUserSalt } returns null

        val p1 = handler.migrate(UnlockRequest.Password(testPassword))
        val p2 = handler.migrate(UnlockRequest.Password(testPassword))

        // Each migration from 1.x.x must produce a fresh random VMK, so wrappedVMKs differ
        assertFalse(
            "Two 1.x.x migrations must wrap different random VMKs",
            p1.wrappedVMK.contentEquals(p2.wrappedVMK)
        )
    }

    @Test
    fun `migrate from 1xx result can be unlocked with same password`() = runTest {
        every { mockConfig.legacyPasswordHash } returns testBcryptHash
        every { mockConfig.legacyUserSalt } returns null

        val protection = handler.migrate(UnlockRequest.Password(testPassword))
        val vmk = handler.unlock(UnlockRequest.Password(testPassword), protection)

        assertNotNull(vmk)
        assertEquals(32, vmk.encoded.size)
    }

    @Test(expected = Exception::class)
    fun `migrate fails when password does not match legacy bcrypt hash`() = runTest {
        every { mockConfig.legacyPasswordHash } returns testBcryptHash

        handler.migrate(UnlockRequest.Password("totally-wrong-password"))
    }

    // --- migrate from 2.x.x (with legacy salt) ---

    @Test
    fun `migrate from 2xx derives VMK deterministically from legacy salt and password`() = runTest {
        every { mockConfig.legacyPasswordHash } returns testBcryptHash
        every { mockConfig.legacyUserSalt } returns legacySaltBase64

        val p1 = handler.migrate(UnlockRequest.Password(testPassword))
        val p2 = handler.migrate(UnlockRequest.Password(testPassword))

        // Both migrations must unwrap to the same VMK (derived from the same legacy salt)
        val vmk1 = handler.unlock(UnlockRequest.Password(testPassword), p1)
        val vmk2 = handler.unlock(UnlockRequest.Password(testPassword), p2)

        assertArrayEquals(
            "2.x.x migration must produce the same deterministic VMK each time",
            vmk1.encoded,
            vmk2.encoded,
        )
    }

    @Test
    fun `migrate from 2xx VMK matches the original PBKDF2 derived legacy key`() = runTest {
        every { mockConfig.legacyPasswordHash } returns testBcryptHash
        every { mockConfig.legacyUserSalt } returns legacySaltBase64

        // Derive what the 2.x.x user key would have been
        val expectedVmk = keyGen.derivePasswordKeyEncryptionKey(
            password = testPassword,
            salt = legacySalt,
            kdf = dev.leonlatsch.photok.encryption.domain.models.Kdf.PBKDF2WithHmacSHA256,
            kdfIterations = 100_000,
            keySize = 256,
        )

        val protection = handler.migrate(UnlockRequest.Password(testPassword))
        val actualVmk = handler.unlock(UnlockRequest.Password(testPassword), protection)

        assertArrayEquals(
            "VMK after 2.x.x migration must equal the original PBKDF2-derived legacy key",
            expectedVmk.encoded,
            actualVmk.encoded,
        )
    }

    @Test
    fun `migrate from 2xx result can be unlocked with same password`() = runTest {
        every { mockConfig.legacyPasswordHash } returns testBcryptHash
        every { mockConfig.legacyUserSalt } returns legacySaltBase64

        val protection = handler.migrate(UnlockRequest.Password(testPassword))
        val vmk = handler.unlock(UnlockRequest.Password(testPassword), protection)

        assertNotNull(vmk)
        assertEquals(32, vmk.encoded.size)
    }

    // Workaround: JUnit 4 doesn't allow calling Assert.assertEquals directly from extension scope
    private fun assertEquals(expected: Any?, actual: Any?) = org.junit.Assert.assertEquals(expected, actual)
}
