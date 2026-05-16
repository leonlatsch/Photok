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
import dev.leonlatsch.photok.encryption.domain.models.CreateRequest
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionType
import dev.leonlatsch.photok.encryption.domain.models.VaultSession
import dev.leonlatsch.photok.settings.data.Config
import javax.inject.Inject

class VaultService @Inject constructor(
    private val config: Config,
    private val vaultProtectionRepository: VaultProtectionRepository,
    private val passwordProtectionHandler: VaultProtectionHandler<UnlockRequest.Password, CreateRequest.Password>,
    private val biometricProtectionHandler: VaultProtectionHandler<UnlockRequest.Biometric, CreateRequest.Biometric>,
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

    suspend fun create(request: CreateRequest) {
        val protection = when (request) {
            is CreateRequest.Password -> passwordProtectionHandler.create(request)
            is CreateRequest.Biometric -> biometricProtectionHandler.create(request)
        }

        vaultProtectionRepository.createProtection(protection)
    }

    suspend fun isSetup(type: VaultProtectionType): Boolean {
        return vaultProtectionRepository.getProtection(type) != null
    }

    suspend fun migrate(request: CreateRequest) {
        val protection = when (request) {
            is CreateRequest.Password -> passwordProtectionHandler.migrate(request)
            is CreateRequest.Biometric -> biometricProtectionHandler.migrate(request)
        }

        vaultProtectionRepository.createProtection(protection)
    }

    suspend fun needsMigration(): Boolean {
        return !isSetup(VaultProtectionType.Password)
                && config.securityPassword.orEmpty().isEmpty()
                && config.userSalt.orEmpty().isEmpty()
    }
}

