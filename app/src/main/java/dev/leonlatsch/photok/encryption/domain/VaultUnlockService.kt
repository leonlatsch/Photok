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

import dev.leonlatsch.photok.encryption.domain.unlockers.ProtectionUnlocker
import javax.inject.Inject

class VaultUnlockService @Inject constructor(
    private val vaultProtectionRepository: VaultProtectionRepository,
    private val passwordUnlocker: ProtectionUnlocker<UnlockRequest.Password>,
    private val biometricUnlocker: ProtectionUnlocker<UnlockRequest.Biometric>,
) {
    suspend fun unlock(request: UnlockRequest): Result<VaultSession> {
        val type = request.protectionType
        val protection = vaultProtectionRepository.getProtection(type)
        protection ?: return Result.failure(IllegalArgumentException())

        return runCatching {
            val vmk = when (request) {
                is UnlockRequest.Password -> passwordUnlocker.unlock(request, protection)
                is UnlockRequest.Biometric -> biometricUnlocker.unlock(request, protection)
            }

            VaultSession(
                vmk = vmk,
            )
        }
    }
}

