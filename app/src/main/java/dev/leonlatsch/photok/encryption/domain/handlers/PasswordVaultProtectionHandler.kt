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

import dev.leonlatsch.photok.encryption.domain.crypto.IV_SIZE
import dev.leonlatsch.photok.encryption.domain.crypto.KeyGen
import dev.leonlatsch.photok.encryption.domain.crypto.SALT_SIZE
import dev.leonlatsch.photok.encryption.domain.models.Algorithm
import dev.leonlatsch.photok.encryption.domain.models.CreateRequest
import dev.leonlatsch.photok.encryption.domain.models.Kdf
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import dev.leonlatsch.photok.encryption.domain.models.VaultProtection
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionParams
import dev.leonlatsch.photok.settings.data.Config
import org.mindrot.jbcrypt.BCrypt
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import kotlin.io.encoding.Base64

private const val KEK_SIZE = 256
private const val KEK_ITERATIONS = 100_000

class PasswordVaultProtectionHandler @Inject constructor(
    private val keyGen: KeyGen,
    private val config: Config,
) : VaultProtectionHandler<UnlockRequest.Password, CreateRequest.Password> {

    override suspend fun unlock(
        request: UnlockRequest.Password,
        protection: VaultProtection
    ): SecretKey {
        val params = protection.params

        requireNotNull(params.salt)
        requireNotNull(params.iv)
        requireNotNull(params.kdf)
        requireNotNull(params.kdfIterations)
        requireNotNull(params.keySize)
        requireNotNull(params.algorithm)

        val kek = keyGen.derivePasswordKeyEncryptionKey(
            password = request.password,
            salt = Base64.decode(params.salt),
            kdf = params.kdf,
            kdfIterations = params.kdfIterations,
            keySize = params.keySize,
        )

        val cipher = Cipher.getInstance(params.algorithm.value).apply {
            val iv = Base64.decode(params.iv)
            init(Cipher.DECRYPT_MODE, kek, IvParameterSpec(iv))
        }

        val vmkBytes = cipher.doFinal(protection.wrappedVMK)
        return SecretKeySpec(vmkBytes, "AES")
    }

    override suspend fun create(request: CreateRequest.Password): VaultProtection {

        val salt = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }

        val kdf = Kdf.PBKDF2WithHmacSHA256

        val params = VaultProtectionParams(
            salt = Base64.encode(salt),
            iv = Base64.encode(iv),
            kdf = kdf,
            kdfIterations = KEK_ITERATIONS,
            algorithm = Algorithm.AesCbcPkcs7Padding,
            keySize = KEK_SIZE,
        )

        val vmk = keyGen.generateVaultMasterKey()

        val kek = keyGen.derivePasswordKeyEncryptionKey(
            password = request.password,
            salt = salt,
            kdf = kdf,
            kdfIterations = KEK_ITERATIONS,
            keySize = params.keySize,
        )

        val cipher = Cipher.getInstance(params.algorithm.value).apply {
            init(Cipher.ENCRYPT_MODE, kek, IvParameterSpec(iv))
        }

        val wrappedVmk = cipher.doFinal(vmk.encoded)

        return VaultProtection(
            id = UUID.randomUUID().toString(),
            type = request.protectionType,
            wrappedVMK = wrappedVmk,
            params = params,
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
                kdfIterations = KEK_ITERATIONS,
                keySize = KEK_SIZE,
            )
        }

        val salt = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }

        val params = VaultProtectionParams(
            salt = Base64.encode(salt),
            iv = Base64.encode(iv),
            kdf = Kdf.PBKDF2WithHmacSHA256,
            kdfIterations = KEK_ITERATIONS,
            algorithm = Algorithm.AesCbcPkcs7Padding,
            keySize = KEK_SIZE,
        )

        val kek = keyGen.derivePasswordKeyEncryptionKey(
            password = request.password,
            salt = salt,
            kdf = params.kdf!!,
            kdfIterations = params.kdfIterations!!,
            keySize = params.keySize,
        )

        val cipher = Cipher.getInstance(params.algorithm.value).apply {
            init(Cipher.ENCRYPT_MODE, kek, IvParameterSpec(iv))
        }

        val wrappedVmk = cipher.doFinal(vmk.encoded)

        return VaultProtection(
            id = UUID.randomUUID().toString(),
            type = request.protectionType,
            wrappedVMK = wrappedVmk,
            params = params,
        )
    }

    override suspend fun reset() {}
}