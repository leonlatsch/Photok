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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
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
    val currentVault = MutableStateFlow<String?>(null)

    suspend fun verifyCurrent(password: String): Result<Unit> {
        val currentUuid = currentVault.value ?: return Result.failure(NoSuchElementException())

        val vault = vaultRepository.get(currentUuid)
        vault ?: return Result.failure(NoSuchElementException())

        val userKey = deriveUserKey(password, vault.salt)
        val iv = vault.iv

        val verifier = try {
            Cipher.getInstance(AES_ALGORITHM).let { cipher ->
                cipher.init(Cipher.DECRYPT_MODE, userKey, IvParameterSpec(iv))
                cipher.doFinal(vault.verifier)
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (!verifier.contentEquals(VERIFIER_PLAINTEXT)) {
            return Result.failure(SecurityException())
        }

        return Result.success(Unit)
    }

    suspend fun tryUnlock(password: String): Result<SecretKey> {
        val vaults = vaultRepository.getAll()

        for (vault in vaults) {
            runCatching {
                val userKey = deriveUserKey(password, vault.salt)
                val iv = vault.iv

                val plaintext = Cipher.getInstance(AES_ALGORITHM).let { cipher ->
                    cipher.init(Cipher.DECRYPT_MODE, userKey, IvParameterSpec(iv))
                    cipher.doFinal(vault.verifier)
                }

                if (plaintext.contentEquals(VERIFIER_PLAINTEXT)) {
                    val cipher = Cipher.getInstance(AES_ALGORITHM)
                    cipher.init(Cipher.DECRYPT_MODE, userKey, IvParameterSpec(iv))

                    val plainContentKey = cipher.doFinal(vault.contentKey)
                    return Result.success(SecretKeySpec(plainContentKey, AES)).also {
                        currentVault.update { vault.uuid }
                    }
                }
            }
        }

        currentVault.update { null }
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
            iv = iv,
        )

        vaultRepository.create(vault)

        return Result.success(contentKey)
    }

    @Suppress("DEPRECATION")
    suspend fun migrateFromPassword(password: String) = runCatching {
        vaultRepository.removeAll()

        val encodedSalt = config.legacyUserSalt ?: throw IllegalStateException("User salt is null")
        val salt = Base64.decode(encodedSalt)

        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }

        val userKey = deriveUserKey(password, salt)
        val plainContentKey =
            userKey // use old user key as content key, so we don't need to re encrypt

        val verifier = Cipher.getInstance(AES_ALGORITHM).let { cipher ->
            cipher.init(Cipher.ENCRYPT_MODE, userKey, IvParameterSpec(iv))
            cipher.doFinal(VERIFIER_PLAINTEXT)
        }

        val encryptedContentKey = Cipher.getInstance(AES_ALGORITHM).let { cipher ->
            cipher.init(Cipher.ENCRYPT_MODE, userKey, IvParameterSpec(iv))
            cipher.doFinal(plainContentKey.encoded)
        }

        val vault = Vault(
            uuid = UUID.randomUUID().toString(),
            salt = salt,
            contentKey = encryptedContentKey,
            verifier = verifier,
            iv = iv,
        )

        vaultRepository.create(vault)

        config.legacyPassword = null
        config.legacyUserSalt = null
    }

    @Suppress("DEPRECATION")
    suspend fun needsMigration(): Boolean {
        return !vaultRepository.hasVaults() && config.legacyPassword != null
    }

    suspend fun changeKey(oldPassword: String, newPassword: String): Result<Unit> = runCatching {
        val currentUuid = currentVault.value ?: throw NoSuchElementException()

        val vault = vaultRepository.get(currentUuid)
        vault ?: throw NoSuchElementException()

        val oldUserKey = deriveUserKey(oldPassword, vault.salt)
        val iv = vault.iv

        val plainContentKey = Cipher.getInstance(AES_ALGORITHM).let { cipher ->
            cipher.init(Cipher.DECRYPT_MODE, oldUserKey, IvParameterSpec(iv))
            cipher.doFinal(vault.contentKey)
        }

        val newSalt = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }
        val newUserKey = deriveUserKey(newPassword, newSalt)


        val verifier = Cipher.getInstance(AES_ALGORITHM).let { cipher ->
            cipher.init(Cipher.ENCRYPT_MODE, newUserKey, IvParameterSpec(iv))
            cipher.doFinal(VERIFIER_PLAINTEXT)
        }

        val encryptedContentKey = Cipher.getInstance(AES_ALGORITHM).let { cipher ->
            cipher.init(Cipher.ENCRYPT_MODE, newUserKey, IvParameterSpec(iv))
            cipher.doFinal(plainContentKey)
        }

        val newVault = Vault(
            uuid = vault.uuid,
            salt = newSalt,
            contentKey = encryptedContentKey,
            verifier = verifier,
            iv = iv,
        )

        vaultRepository.update(newVault)
        currentVault.update { newVault.uuid }
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

