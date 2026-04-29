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

import dev.leonlatsch.photok.encryption.domain.handlers.VaultProtectionHandler
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import dev.leonlatsch.photok.encryption.domain.models.VaultSession
import javax.inject.Inject

// TODO: Until now this is kind of only a proxy that does the when block ... meh
class VaultUnlockService @Inject constructor(
    private val vaultProtectionRepository: VaultProtectionRepository,
    private val passwordProtectionHandler: VaultProtectionHandler<UnlockRequest.Password>,
    private val biometricProtectionHandler: VaultProtectionHandler<UnlockRequest.Biometric>,
) {
    suspend fun unlock(request: UnlockRequest): Result<VaultSession> {
        val type = request.protectionType
        val protection = vaultProtectionRepository.getProtection(type)
        protection ?: return Result.failure(IllegalArgumentException())

        return runCatching {
            val vmk = when (request) {
                is UnlockRequest.Password -> passwordProtectionHandler.unlock(request, protection)
                is UnlockRequest.Biometric -> biometricProtectionHandler.unlock(request, protection)
            }

            VaultSession(
                vmk = vmk,
            )
        }
    }

    suspend fun create(request: UnlockRequest) {
        val protection = when (request) {
            is UnlockRequest.Password -> passwordProtectionHandler.create(request)
            is UnlockRequest.Biometric -> biometricProtectionHandler.create(request)
        }

        vaultProtectionRepository.createProtection(protection)
    }
}

