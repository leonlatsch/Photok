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

import dev.leonlatsch.photok.encryption.domain.crypto.IV_SIZE
import dev.leonlatsch.photok.encryption.domain.crypto.KeyGen
import dev.leonlatsch.photok.encryption.domain.crypto.SALT_SIZE
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionType
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import kotlin.io.encoding.Base64

class ChangePasswordUseCase @Inject constructor(
    private val vaultProtectionRepository: VaultProtectionRepository,
    private val sessionRepository: SessionRepository,
    private val keyGen: KeyGen,
) {
    suspend operator fun invoke(newPassword: String) = runCatching {
        val currentProtection = vaultProtectionRepository.getProtection(VaultProtectionType.Password)
        requireNotNull(currentProtection)

        val session = sessionRepository.require()

        val newSalt = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }
        val newIv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }

        val newKek = keyGen.derivePasswordKeyEncryptionKey(
            password = newPassword,
            salt = newSalt,
            kdf = requireNotNull(currentProtection.params.kdf) { "Password protection is missing KDF" },
            kdfIterations = requireNotNull(currentProtection.params.kdfIterations) { "Password protection is missing KDF iterations" },
            keySize = currentProtection.params.keySize,
        )

        val cipher = Cipher.getInstance(currentProtection.params.algorithm.value).apply {
            init(Cipher.ENCRYPT_MODE, newKek, IvParameterSpec(newIv))
        }

        val newWrappedVmk = cipher.doFinal(session.vmk.encoded)

        val newParams = currentProtection.params.copy(
            salt = Base64.encode(newSalt),
            iv = Base64.encode(newIv),
        )

        val newProtection = currentProtection.copy(
            wrappedVMK = newWrappedVmk,
            params = newParams,
        )

        vaultProtectionRepository.updateProtection(newProtection)
    }
}