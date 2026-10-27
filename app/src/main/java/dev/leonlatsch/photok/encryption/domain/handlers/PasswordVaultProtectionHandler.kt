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

import dev.leonlatsch.photok.encryption.domain.PasswordVmkWrapper
import dev.leonlatsch.photok.encryption.domain.crypto.KeyGen
import dev.leonlatsch.photok.encryption.domain.models.CreateRequest
import dev.leonlatsch.photok.encryption.domain.models.Kdf
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import dev.leonlatsch.photok.encryption.domain.models.VaultProtection
import dev.leonlatsch.photok.settings.data.Config
import org.mindrot.jbcrypt.BCrypt
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import kotlin.io.encoding.Base64

private const val LEGACY_V2_KEY_SIZE = 256
private const val LEGACY_V2_KDF_ITERATIONS = 100_000

class PasswordVaultProtectionHandler @Inject constructor(
    private val keyGen: KeyGen,
    private val vmkWrapper: PasswordVmkWrapper,
    private val config: Config,
) : VaultProtectionHandler<UnlockRequest.Password, CreateRequest.Password> {

    override suspend fun unlock(
        request: UnlockRequest.Password,
        protection: VaultProtection
    ): SecretKey {
        val params = protection.params

        val salt = requireNotNull(params.salt)
        val kdf = requireNotNull(params.kdf)
        val kdfIterations = requireNotNull(params.kdfIterations)

        val kek = keyGen.derivePasswordKeyEncryptionKey(
            password = request.password,
            salt = Base64.decode(salt),
            kdf = kdf,
            kdfIterations = kdfIterations,
            keySize = params.keySize,
        )

        val cipher = Cipher.getInstance(params.algorithm.value).apply {
            val iv = Base64.decode(params.iv)
            init(Cipher.DECRYPT_MODE, kek, IvParameterSpec(iv))
        }

        val vmkBytes = cipher.doFinal(protection.wrappedVMK)
        return toVaultMasterKey(vmkBytes)
    }

    override suspend fun create(request: CreateRequest.Password): VaultProtection {
        val wrapped = vmkWrapper.wrap(request.password, keyGen.generateVaultMasterKey())

        return VaultProtection(
            id = UUID.randomUUID().toString(),
            type = request.protectionType,
            wrappedVMK = wrapped.wrappedVMK,
            params = wrapped.params,
        )
    }

    override suspend fun canMigrate(): Boolean {
        // 1.x.x users have no legacyUserSalt — migrate() handles that case by generating a fresh
        // VMK. Returning true when only legacyPasswordHash is present covers both 1.x.x and 2.x.x.
        return config.legacyPasswordHash.orEmpty().isNotEmpty()
    }

    override suspend fun migrate(request: UnlockRequest.Password): VaultProtection {
        require(BCrypt.checkpw(request.password, config.legacyPasswordHash))

        val vmk = if (config.legacyUserSalt.isNullOrEmpty()) {
            // Migrating from 1.x.x
            keyGen.generateVaultMasterKey()
        } else {
            // Migrating from 2.x.x
            val vmkSalt = Base64.decode(config.legacyUserSalt!!)

            keyGen.derivePasswordKeyEncryptionKey(
                password = request.password,
                salt = vmkSalt,
                kdf = Kdf.PBKDF2WithHmacSHA256,
                kdfIterations = LEGACY_V2_KDF_ITERATIONS,
                keySize = LEGACY_V2_KEY_SIZE,
            )
        }

        val wrapped = vmkWrapper.wrap(request.password, vmk)

        return VaultProtection(
            id = UUID.randomUUID().toString(),
            type = request.protectionType,
            wrappedVMK = wrapped.wrappedVMK,
            params = wrapped.params,
        )
    }

    override suspend fun reset() {}
}
