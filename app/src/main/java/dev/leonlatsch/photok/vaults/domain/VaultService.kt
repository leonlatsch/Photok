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

package dev.leonlatsch.photok.vaults.domain

import dev.leonlatsch.photok.security.AES
import dev.leonlatsch.photok.security.AES_ALGORITHM
import dev.leonlatsch.photok.security.ITERATION_COUNT
import dev.leonlatsch.photok.security.IV_SIZE
import dev.leonlatsch.photok.security.KEY_ALGORITHM
import dev.leonlatsch.photok.security.KEY_SIZE
import dev.leonlatsch.photok.security.SALT_SIZE
import dev.leonlatsch.photok.security.VERIFIER_PLAINTEXT
import dev.leonlatsch.photok.settings.data.Config
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64

@Singleton
class VaultService @Inject constructor(
    private val vaultRepository: VaultRepository,
    private val config: Config,
) {
    suspend fun tryUnlock(password: String): Result<SecretKey> {
        val vaults = vaultRepository.getAll()

        for (vault in vaults) {
            val userKey = deriveUserKey(password, vault.salt)
            val iv = vault.verifierIv
            val cipher = Cipher.getInstance(AES_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, userKey, IvParameterSpec(iv))

            runCatching {
                val plaintext = cipher.doFinal(vault.verifier)
                if (plaintext.contentEquals(VERIFIER_PLAINTEXT)) {
                    val cipher = Cipher.getInstance(AES_ALGORITHM)
                    cipher.init(Cipher.DECRYPT_MODE, userKey, IvParameterSpec(iv))

                    val plainContentKey = cipher.doFinal(vault.contentKey)
                    return Result.success(SecretKeySpec(plainContentKey, AES))
                }
            }
        }

        return Result.failure(NoSuchElementException())
    }

    suspend fun createVault(password: String): Result<SecretKey> {
        val salt = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }

        val userKey = deriveUserKey(password, salt)
        val contentKey = generateContentKey()

        val verifier = Cipher.getInstance(AES_ALGORITHM).let { cipher ->
            cipher.init(Cipher.ENCRYPT_MODE, userKey, IvParameterSpec(iv))
            cipher.doFinal(VERIFIER_PLAINTEXT)
        }

        val encryptedContentKey = Cipher.getInstance(AES_ALGORITHM).let { cipher ->
            cipher.init(Cipher.ENCRYPT_MODE, userKey, IvParameterSpec(iv))
            cipher.doFinal(contentKey.encoded)
        }

        val vault = Vault(
            uuid = UUID.randomUUID().toString(),
            salt = salt,
            contentKey = encryptedContentKey,
            verifier = verifier,
            verifierIv = iv,
        )

        vaultRepository.create(vault)

        return Result.success(contentKey)
    }

    @Suppress("DEPRECATION")
    suspend fun migrateVaultIfNeeded(password: String) = runCatching {
        if (vaultRepository.hasVaults()) {
            return@runCatching
        }

        if (config.securityPassword == null) {
            return@runCatching
        }

        val encodedOldSalt = config.userSalt ?: throw IllegalStateException("User salt is null")

        val oldSalt = Base64.decode(encodedOldSalt)

        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }
        val salt = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }

        val userKey = deriveUserKey(password, oldSalt)
        val contentKey = userKey // use old user key as content key, so we don't need to re encrypt

        val verifier = Cipher.getInstance(AES_ALGORITHM).let { cipher ->
            cipher.init(Cipher.ENCRYPT_MODE, userKey, IvParameterSpec(iv))
            cipher.doFinal(VERIFIER_PLAINTEXT)
        }

        val encryptedContentKey = Cipher.getInstance(AES_ALGORITHM).let { cipher ->
            cipher.init(Cipher.ENCRYPT_MODE, userKey, IvParameterSpec(iv))
            cipher.doFinal(contentKey.encoded)
        }

        val vault = Vault(
            uuid = UUID.randomUUID().toString(),
            salt = salt,
            contentKey = encryptedContentKey,
            verifier = verifier,
            verifierIv = iv,
        )

        vaultRepository.create(vault)

        config.securityPassword = null
        config.userSalt = null
    }

    private fun deriveUserKey(password: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance(KEY_ALGORITHM)
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_SIZE)
        val keyBytes = factory.generateSecret(spec).encoded

        return SecretKeySpec(keyBytes, AES)
    }

    private fun generateContentKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(AES)
        val secureRandom = SecureRandom()
        keyGenerator.init(KEY_SIZE, secureRandom)
        return keyGenerator.generateKey()
    }
}

