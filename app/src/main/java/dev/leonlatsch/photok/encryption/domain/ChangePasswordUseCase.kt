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

import dev.leonlatsch.photok.encryption.domain.handlers.PasswordVmkWrapper
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionType
import dev.leonlatsch.photok.settings.data.Config
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val vaultProtectionRepository: VaultProtectionRepository,
    private val sessionRepository: SessionRepository,
    private val vmkWrapper: PasswordVmkWrapper,
    private val config: Config,
) {
    suspend operator fun invoke(newPassword: String) = runCatching {
        val currentProtection = vaultProtectionRepository.getProtection(VaultProtectionType.Password)
        requireNotNull(currentProtection)

        val session = requireNotNull(sessionRepository.get()) { "Vault is locked" }

        val wrapped = vmkWrapper.wrap(newPassword, session.vmk)

        val newProtection = currentProtection.copy(
            wrappedVMK = wrapped.wrappedVMK,
            params = wrapped.params,
        )

        vaultProtectionRepository.updateProtection(newProtection)
        config.lastUsedUnlockMethod = newProtection.type
    }
}
