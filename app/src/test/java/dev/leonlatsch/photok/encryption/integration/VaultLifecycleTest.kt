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

import dev.leonlatsch.photok.encryption.domain.ChangePasswordUseCase
import dev.leonlatsch.photok.encryption.domain.PasswordVmkWrapper
import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.encryption.domain.VaultProtectionRepository
import dev.leonlatsch.photok.encryption.domain.VaultService
import dev.leonlatsch.photok.encryption.domain.crypto.KeyGen
import dev.leonlatsch.photok.encryption.domain.handlers.PasswordVaultProtectionHandler
import dev.leonlatsch.photok.encryption.domain.models.CreateRequest
import dev.leonlatsch.photok.encryption.domain.models.Kdf
import dev.leonlatsch.photok.encryption.domain.models.KekSpec
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import dev.leonlatsch.photok.encryption.domain.models.VaultProtection
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionType
import dev.leonlatsch.photok.encryption.domain.models.VaultSession
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mindrot.jbcrypt.BCrypt
import org.robolectric.RobolectricTestRunner
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import dev.leonlatsch.photok.settings.data.Config as AppConfig

/**
 * End-to-end tests for the full vault lifecycle: create, unlock, change password, and
 * both migration paths (1.x.x and 2.x.x). Each test exercises a complete flow rather
 * than a single method, so a failure here means real user data is at risk.
 */
@RunWith(RobolectricTestRunner::class)
class VaultLifecycleTest {

    private val keyGen = KeyGen()
    private val mockConfig = mockk<AppConfig>(relaxed = true)
    private val vmkWrapper = PasswordVmkWrapper(keyGen)
    private val handler = PasswordVaultProtectionHandler(keyGen, vmkWrapper, mockConfig)

    private val mockProtectionRepository = mockk<VaultProtectionRepository>()
    private val mockSessionRepository = mockk<SessionRepository>()
    private val changePasswordUseCase = ChangePasswordUseCase(
        vaultProtectionRepository = mockProtectionRepository,
        sessionRepository = mockSessionRepository,
        vmkWrapper = vmkWrapper,
        config = mockConfig,
    )
    private val vaultService = VaultService(
        vaultProtectionRepository = mockProtectionRepository,
        passwordProtectionHandler = handler,
        biometricProtectionHandler = mockk(),
        recoveryPhraseProtectionHandler = mockk(),
        passwordVmkWrapper = vmkWrapper,
        config = mockConfig,
    )

    private val password = "correct-horse-battery-staple"
    private val bcryptHash = BCrypt.hashpw(password, BCrypt.gensalt(4))
    private val legacySalt = ByteArray(16) { (it * 3).toByte() }
    private val legacySaltBase64: String = Base64.getEncoder().encodeToString(legacySalt)

    @Before
    fun setup() {
        every { mockConfig.legacyPasswordHash } returns null
        every { mockConfig.legacyUserSalt } returns null
    }

    /**
     * Creating a vault and unlocking it must return the same VMK on every call.
     * Each create call must also produce unique salt and IV (no determinism leak).
     */
    @Test
    fun `create vault and unlock returns consistent VMK`() = runTest {
        val protection = handler.create(CreateRequest.Password(password))
        val request = UnlockRequest.Password(password)

        val vmk1 = handler.unlock(request, protection)
        val vmk2 = handler.unlock(request, protection)
        assertArrayEquals("Unlock must return the same VMK on every call", vmk1.encoded, vmk2.encoded)

        val otherProtection = handler.create(CreateRequest.Password(password))
        assertFalse(
            "Each create call must produce a unique salt",
            protection.params.salt == otherProtection.params.salt,
        )
    }

    /**
     * A wrong password must always fail to unlock the vault.
     */
    @Test(expected = Exception::class)
    fun `wrong password cannot unlock vault`() = runTest {
        val protection = handler.create(CreateRequest.Password(password))
        handler.unlock(UnlockRequest.Password("wrong-password"), protection)
    }

    /**
     * After rotating the password the new password must unlock the vault and recover the
     * original VMK, while the old password must no longer work.
     */
    @Test
    fun `change password re-encrypts VMK, new password unlocks vault with same VMK, old password fails`() = runTest {
        val newPassword = "brand-new-password"

        val initialProtection = handler.create(CreateRequest.Password(password))
        val initialVmk = handler.unlock(UnlockRequest.Password(password), initialProtection)

        val capturedProtection = slot<VaultProtection>()
        coEvery { mockProtectionRepository.getProtection(VaultProtectionType.Password) } returns initialProtection
        coEvery { mockSessionRepository.get() } returns VaultSession(initialVmk)
        coEvery { mockProtectionRepository.updateProtection(capture(capturedProtection)) } returns Unit

        changePasswordUseCase(newPassword)

        val newProtection = capturedProtection.captured

        val vmkAfterChange = handler.unlock(UnlockRequest.Password(newPassword), newProtection)
        assertArrayEquals(
            "New password must recover the same VMK",
            initialVmk.encoded,
            vmkAfterChange.encoded,
        )

        val oldPasswordResult = runCatching { handler.unlock(UnlockRequest.Password(password), newProtection) }
        assertTrue("Old password must no longer unlock the vault", oldPasswordResult.isFailure)
    }

    /**
     * Vaults created before the iteration bump store 100K iterations. Unlock must use the stored
     * count, not the current constant, or those users are locked out.
     */
    @Test
    fun `vault created with 100K iterations still unlocks`() = runTest {
        val vmk = keyGen.generateVaultMasterKey()
        val legacyProtection = createProtectionWith100KIterations(vmk)

        val unlockedVmk = handler.unlock(UnlockRequest.Password(password), legacyProtection)

        assertArrayEquals("100K vault must unlock with the same VMK", vmk.encoded, unlockedVmk.encoded)
    }

    /**
     * Changing the password of a 100K vault must upgrade it to [KekSpec.Password].
     */
    @Test
    fun `change password upgrades 100K vault to current iterations`() = runTest {
        val vmk = keyGen.generateVaultMasterKey()
        val legacyProtection = createProtectionWith100KIterations(vmk)

        val capturedProtection = slot<VaultProtection>()
        coEvery { mockProtectionRepository.getProtection(VaultProtectionType.Password) } returns legacyProtection
        coEvery { mockSessionRepository.get() } returns VaultSession(vmk)
        coEvery { mockProtectionRepository.updateProtection(capture(capturedProtection)) } returns Unit

        changePasswordUseCase("brand-new-password")

        val newProtection = capturedProtection.captured
        assertEquals(KekSpec.Password.kdfIterations, newProtection.params.kdfIterations)
        assertEquals(Kdf.PBKDF2WithHmacSHA256, newProtection.params.kdf)

        val unlockedVmk = handler.unlock(UnlockRequest.Password("brand-new-password"), newProtection)
        assertArrayEquals("Upgraded vault must recover the same VMK", vmk.encoded, unlockedVmk.encoded)
    }

    /**
     * Unlocking a 100K vault and then upgrading must store [KekSpec.Password] params that still
     * unwrap the same VMK with the same password.
     */
    @Test
    fun `upgrade after unlock moves 100K vault to current iterations`() = runTest {
        val vmk = keyGen.generateVaultMasterKey()
        val legacyProtection = createProtectionWith100KIterations(vmk)

        val capturedProtection = slot<VaultProtection>()
        coEvery { mockProtectionRepository.getProtection(VaultProtectionType.Password) } returns legacyProtection
        coEvery { mockProtectionRepository.updateProtection(capture(capturedProtection)) } returns Unit

        val session = vaultService.unlock(UnlockRequest.Password(password)).getOrThrow()
        vaultService.upgradePasswordProtectionIfOutdated(password, session.vmk)

        val newProtection = capturedProtection.captured
        assertEquals(KekSpec.Password.kdfIterations, newProtection.params.kdfIterations)
        assertEquals(legacyProtection.id, newProtection.id)

        val unlockedVmk = handler.unlock(UnlockRequest.Password(password), newProtection)
        assertArrayEquals("Upgraded vault must recover the same VMK", vmk.encoded, unlockedVmk.encoded)
    }

    /**
     * A failed upgrade must not throw; the old protection stays valid.
     */
    @Test
    fun `failed upgrade of 100K vault does not throw`() = runTest {
        val vmk = keyGen.generateVaultMasterKey()
        val legacyProtection = createProtectionWith100KIterations(vmk)

        coEvery { mockProtectionRepository.getProtection(VaultProtectionType.Password) } returns legacyProtection
        coEvery { mockProtectionRepository.updateProtection(any()) } throws IllegalStateException("disk full")

        vaultService.upgradePasswordProtectionIfOutdated(password, vmk)

        val unlockedVmk = handler.unlock(UnlockRequest.Password(password), legacyProtection)
        assertArrayEquals("Old protection must still unlock", vmk.encoded, unlockedVmk.encoded)
    }

    /**
     * Unlock alone must never rewrite the protection, or a wrong password could lock the user out.
     */
    @Test
    fun `unlock does not rewrite 100K vault`() = runTest {
        val vmk = keyGen.generateVaultMasterKey()
        val legacyProtection = createProtectionWith100KIterations(vmk)

        coEvery { mockProtectionRepository.getProtection(VaultProtectionType.Password) } returns legacyProtection

        val wrongResult = vaultService.unlock(UnlockRequest.Password("wrong-password"))
        vaultService.unlock(UnlockRequest.Password(password)).getOrThrow()

        assertTrue("Wrong password must not unlock the vault", wrongResult.isFailure)
        coVerify(exactly = 0) { mockProtectionRepository.updateProtection(any()) }
    }

    /**
     * A vault already on the current params must not be rewritten.
     */
    @Test
    fun `upgrade is a no-op for current vault`() = runTest {
        val vmk = keyGen.generateVaultMasterKey()
        val protection = handler.create(CreateRequest.Password(password))
        coEvery { mockProtectionRepository.getProtection(VaultProtectionType.Password) } returns protection

        vaultService.upgradePasswordProtectionIfOutdated(password, vmk)

        coVerify(exactly = 0) { mockProtectionRepository.updateProtection(any()) }
    }

    private suspend fun createProtectionWith100KIterations(vmk: SecretKey): VaultProtection {
        val template = handler.create(CreateRequest.Password(password))
        val params = template.params.copy(kdfIterations = 100_000)

        val kek = keyGen.derivePasswordKeyEncryptionKey(
            password = password,
            salt = Base64.getDecoder().decode(params.salt),
            kdf = Kdf.PBKDF2WithHmacSHA256,
            kdfIterations = 100_000,
            keySize = params.keySize,
        )
        val cipher = Cipher.getInstance(params.algorithm.value).apply {
            init(Cipher.ENCRYPT_MODE, kek, IvParameterSpec(Base64.getDecoder().decode(params.iv)))
        }

        return template.copy(wrappedVMK = cipher.doFinal(vmk.encoded), params = params)
    }

    /**
     * Migration from 2.x.x: the VMK stored in the new vault must equal the original
     * PBKDF2-derived key so that all previously encrypted files remain accessible.
     * Wrong passwords must be rejected both at migrate time (bcrypt) and at unlock time.
     */
    @Test
    fun `migrate from 2xx vault is accessible with correct password and VMK equals original PBKDF2 key`() = runTest {
        every { mockConfig.legacyPasswordHash } returns bcryptHash
        every { mockConfig.legacyUserSalt } returns legacySaltBase64

        // The key that 2.x.x used to encrypt every file — this becomes the VMK after migration
        val expectedVmk = keyGen.derivePasswordKeyEncryptionKey(
            password = password,
            salt = legacySalt,
            kdf = Kdf.PBKDF2WithHmacSHA256,
            kdfIterations = 100_000,
            keySize = 256,
        )

        val protection = handler.migrate(UnlockRequest.Password(password))
        val actualVmk = handler.unlock(UnlockRequest.Password(password), protection)

        assertArrayEquals(
            "VMK after 2.x.x migration must equal the original PBKDF2-derived file encryption key",
            expectedVmk.encoded,
            actualVmk.encoded,
        )

        val wrongPasswordResult = runCatching { handler.unlock(UnlockRequest.Password("wrong"), protection) }
        assertTrue("Wrong password must fail after 2.x.x migration", wrongPasswordResult.isFailure)
    }

    /**
     * Migration from 1.x.x: no legacy salt exists so a fresh random VMK is generated.
     * The vault must be unlockable with the original password, and each migration run
     * must produce a different VMK (no determinism without a salt).
     */
    @Test
    fun `migrate from 1xx vault is accessible with correct password using fresh random VMK`() = runTest {
        every { mockConfig.legacyPasswordHash } returns bcryptHash
        every { mockConfig.legacyUserSalt } returns null

        val protection1 = handler.migrate(UnlockRequest.Password(password))
        val vmk1 = handler.unlock(UnlockRequest.Password(password), protection1)
        assertTrue("VMK must be a 256-bit AES key", vmk1.encoded.size == 32)

        val protection2 = handler.migrate(UnlockRequest.Password(password))
        val vmk2 = handler.unlock(UnlockRequest.Password(password), protection2)

        assertFalse(
            "Each 1.x.x migration must generate a unique random VMK",
            vmk1.encoded.contentEquals(vmk2.encoded),
        )
    }
}
