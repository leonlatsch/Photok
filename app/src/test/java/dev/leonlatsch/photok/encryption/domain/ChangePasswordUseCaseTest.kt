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

package dev.leonlatsch.photok.encryption.domain

import dev.leonlatsch.photok.encryption.domain.crypto.KeyGen
import dev.leonlatsch.photok.encryption.domain.models.Algorithm
import dev.leonlatsch.photok.encryption.domain.models.CreateRequest
import dev.leonlatsch.photok.encryption.domain.models.Kdf
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import dev.leonlatsch.photok.encryption.domain.models.VaultProtection
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionParams
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionType
import dev.leonlatsch.photok.encryption.domain.models.VaultSession
import dev.leonlatsch.photok.settings.data.Config
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Base64
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

@RunWith(RobolectricTestRunner::class)
class ChangePasswordUseCaseTest {

    private val keyGen = KeyGen()
    private val mockProtectionRepository = mockk<VaultProtectionRepository>()
    private val mockSessionRepository = mockk<SessionRepository>()

    private val useCase = ChangePasswordUseCase(
        vaultProtectionRepository = mockProtectionRepository,
        sessionRepository = mockSessionRepository,
        keyGen = keyGen,
    )

    private val testPassword = "original-password"
    private val newPassword = "brand-new-password"

    @Test
    fun `invoke re-encrypts VMK with new password`() = runTest {
        val (vmk, protection, session) = buildInitialState(testPassword)

        val capturedProtection = slot<VaultProtection>()
        coEvery { mockProtectionRepository.getProtection(VaultProtectionType.Password) } returns protection
        coEvery { mockSessionRepository.require() } returns session
        coEvery { mockProtectionRepository.updateProtection(capture(capturedProtection)) } returns Unit

        useCase(newPassword)

        coVerify { mockProtectionRepository.updateProtection(any()) }

        val newProtection = capturedProtection.captured
        assertFalse(
            "WrappedVMK must change after password change",
            newProtection.wrappedVMK.contentEquals(protection.wrappedVMK),
        )
    }

    @Test
    fun `invoke updates salt and IV`() = runTest {
        val (vmk, protection, session) = buildInitialState(testPassword)

        val capturedProtection = slot<VaultProtection>()
        coEvery { mockProtectionRepository.getProtection(VaultProtectionType.Password) } returns protection
        coEvery { mockSessionRepository.require() } returns session
        coEvery { mockProtectionRepository.updateProtection(capture(capturedProtection)) } returns Unit

        useCase(newPassword)

        val newProtection = capturedProtection.captured
        assertFalse(
            "Salt must be refreshed after password change",
            newProtection.params.salt == protection.params.salt,
        )
        assertFalse(
            "IV must be refreshed after password change",
            newProtection.params.iv == protection.params.iv,
        )
    }

    @Test
    fun `new password successfully decrypts the updated wrappedVMK`() = runTest {
        val (vmk, protection, session) = buildInitialState(testPassword)

        val capturedProtection = slot<VaultProtection>()
        coEvery { mockProtectionRepository.getProtection(VaultProtectionType.Password) } returns protection
        coEvery { mockSessionRepository.require() } returns session
        coEvery { mockProtectionRepository.updateProtection(capture(capturedProtection)) } returns Unit

        useCase(newPassword)

        val newProtection = capturedProtection.captured

        // Manually decrypt wrappedVMK with new password to verify
        val newSalt = Base64.getDecoder().decode(newProtection.params.salt)
        val newIv = Base64.getDecoder().decode(newProtection.params.iv)
        val newKek = keyGen.derivePasswordKeyEncryptionKey(
            password = newPassword,
            salt = newSalt,
            kdf = newProtection.params.kdf!!,
            kdfIterations = newProtection.params.kdfIterations!!,
            keySize = newProtection.params.keySize,
        )
        val cipher = Cipher.getInstance(newProtection.params.algorithm.value).apply {
            init(Cipher.DECRYPT_MODE, newKek, IvParameterSpec(newIv))
        }
        val decryptedVmk = cipher.doFinal(newProtection.wrappedVMK)

        assertArrayEquals("New password must decrypt to original VMK", vmk.encoded, decryptedVmk)
    }

    @Test
    fun `old password cannot decrypt the updated wrappedVMK`() = runTest {
        val (vmk, protection, session) = buildInitialState(testPassword)

        val capturedProtection = slot<VaultProtection>()
        coEvery { mockProtectionRepository.getProtection(VaultProtectionType.Password) } returns protection
        coEvery { mockSessionRepository.require() } returns session
        coEvery { mockProtectionRepository.updateProtection(capture(capturedProtection)) } returns Unit

        useCase(newPassword)

        val newProtection = capturedProtection.captured

        val oldSalt = Base64.getDecoder().decode(newProtection.params.salt)
        val oldIv = Base64.getDecoder().decode(newProtection.params.iv)
        val oldKek = keyGen.derivePasswordKeyEncryptionKey(
            password = testPassword, // old password
            salt = oldSalt,
            kdf = newProtection.params.kdf!!,
            kdfIterations = newProtection.params.kdfIterations!!,
            keySize = newProtection.params.keySize,
        )
        val cipher = Cipher.getInstance(newProtection.params.algorithm.value).apply {
            init(Cipher.DECRYPT_MODE, oldKek, IvParameterSpec(oldIv))
        }

        val result = runCatching { cipher.doFinal(newProtection.wrappedVMK) }
        if (result.isSuccess) {
            assertFalse(
                "Old password must not decrypt to original VMK",
                result.getOrThrow().contentEquals(vmk.encoded),
            )
        }
        // Exception is also acceptable — old KEK can't decrypt new wrappedVMK
    }

    @Test
    fun `invoke fails when no current protection exists`() = runTest {
        coEvery { mockProtectionRepository.getProtection(VaultProtectionType.Password) } returns null

        val result = useCase(newPassword)

        assertTrue("Must fail when no current protection exists", result.isFailure)
    }

    // --- helpers ---

    private data class InitialState(
        val vmk: javax.crypto.SecretKey,
        val protection: VaultProtection,
        val session: VaultSession,
    )

    private fun buildInitialState(password: String): InitialState {
        val vmk = keyGen.generateVaultMasterKey()
        val salt = ByteArray(16) { it.toByte() }
        val iv = ByteArray(16) { (it + 5).toByte() }

        val kek = keyGen.derivePasswordKeyEncryptionKey(
            password = password,
            salt = salt,
            kdf = Kdf.PBKDF2WithHmacSHA256,
            kdfIterations = 1_000, // Low iterations for test speed
            keySize = 256,
        )
        val cipher = Cipher.getInstance(Algorithm.AesCbcPkcs7Padding.value).apply {
            init(Cipher.ENCRYPT_MODE, kek, IvParameterSpec(iv))
        }
        val wrappedVmk = cipher.doFinal(vmk.encoded)

        val protection = VaultProtection(
            id = UUID.randomUUID().toString(),
            type = VaultProtectionType.Password,
            wrappedVMK = wrappedVmk,
            params = VaultProtectionParams(
                salt = Base64.getEncoder().encodeToString(salt),
                iv = Base64.getEncoder().encodeToString(iv),
                kdf = Kdf.PBKDF2WithHmacSHA256,
                kdfIterations = 1_000,
                algorithm = Algorithm.AesCbcPkcs7Padding,
                keySize = 256,
            ),
        )

        return InitialState(vmk, protection, VaultSession(vmk))
    }
}
