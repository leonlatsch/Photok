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

import dev.leonlatsch.photok.security.AES_ALGORITHM
import dev.leonlatsch.photok.security.DeriveAesKeyUseCase
import dev.leonlatsch.photok.security.IV_SIZE
import dev.leonlatsch.photok.security.SALT_SIZE
import dev.leonlatsch.photok.security.VERIFIER_PLAINTEXT
import dev.leonlatsch.photok.settings.data.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.security.KeyException
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64

const val DefaultVaultName = "Default Vault"

@Singleton
class VaultService @Inject constructor(
    private val vaultRepository: VaultRepository,
    private val config: Config,
    private val scope: CoroutineScope,
    private val deriveAesKey: DeriveAesKeyUseCase,
) {
    private val currentVaultId = MutableStateFlow<String?>(null)
    private val vault = currentVaultId.map {
        it ?: return@map null
        vaultRepository.get(it)
    }.stateIn(scope, SharingStarted.Eagerly, null)

    fun getCurrentVault(): Vault? {
        return vault.value
    }

    suspend fun verifyCurrent(password: String): Result<Unit> {
        val currentUuid = currentVaultId.value ?: return Result.failure(NoSuchElementException())

        val vault = vaultRepository.get(currentUuid)
        vault ?: return Result.failure(NoSuchElementException())

        val userKey = deriveAesKey(password, vault.salt)
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

    suspend fun unlock(vault: Vault, password: String): Result<SecretKey> = runCatching {
        val userKey = deriveAesKey(password, vault.salt)
        val iv = vault.iv

        val plainVerifier = Cipher.getInstance(AES_ALGORITHM).let { cipher ->
            cipher.init(Cipher.DECRYPT_MODE, userKey, IvParameterSpec(iv))
            cipher.doFinal(vault.verifier)
        }

        if (!plainVerifier.contentEquals(VERIFIER_PLAINTEXT)) {
            throw KeyException()
        }

        userKey
    }

    suspend fun findAndUnlock(password: String): Result<SecretKey> {
        val vaults = vaultRepository.getAll()

        for (vault in vaults) {
            try {
                val userKey = deriveAesKey(password, vault.salt)
                val iv = vault.iv

                val plainVerifier = Cipher.getInstance(AES_ALGORITHM).let { cipher ->
                    cipher.init(Cipher.DECRYPT_MODE, userKey, IvParameterSpec(iv))
                    cipher.doFinal(vault.verifier)
                }

                if (!plainVerifier.contentEquals(VERIFIER_PLAINTEXT)) {
                    continue
                }

                currentVaultId.update { vault.uuid }
                return Result.success(userKey)
            } catch (e: Exception) {
                continue
            }
        }

        currentVaultId.update { null }
        return Result.failure(NoSuchElementException())
    }

    suspend fun createVault(password: String): Result<SecretKey> {
        val salt = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }

        val userKey = deriveAesKey(password, salt)

        val verifier = Cipher.getInstance(AES_ALGORITHM).let { cipher ->
            cipher.init(Cipher.ENCRYPT_MODE, userKey, IvParameterSpec(iv))
            cipher.doFinal(VERIFIER_PLAINTEXT)
        }

        val vault = Vault(
            uuid = UUID.randomUUID().toString(),
            name = DefaultVaultName,
            salt = salt,
            verifier = verifier,
            iv = iv,
        )

        vaultRepository.create(vault)
        currentVaultId.update { vault.uuid }

        return Result.success(userKey)
    }

    @Suppress("DEPRECATION")
    suspend fun migrateFromPassword(password: String) = runCatching {
        vaultRepository.deleteAll()

        val encodedSalt = config.legacyUserSalt ?: throw IllegalStateException("User salt is null")
        val salt = Base64.decode(encodedSalt)

        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }

        val userKey = deriveAesKey(password, salt)

        val verifier = Cipher.getInstance(AES_ALGORITHM).let { cipher ->
            cipher.init(Cipher.ENCRYPT_MODE, userKey, IvParameterSpec(iv))
            cipher.doFinal(VERIFIER_PLAINTEXT)
        }

        val vault = Vault(
            uuid = UUID.randomUUID().toString(),
            name = DefaultVaultName,
            salt = salt,
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

    suspend fun changePasswordForCurrent(newPassword: String): Result<SecretKey> {
        val vault = getCurrentVault() ?: return Result.failure(NoSuchElementException())

        val userKey = deriveAesKey(newPassword, vault.salt)

        val verifier = Cipher.getInstance(AES_ALGORITHM).let { cipher ->
            cipher.init(Cipher.ENCRYPT_MODE, userKey, IvParameterSpec(vault.iv))
            cipher.doFinal(VERIFIER_PLAINTEXT)
        }

        val updatedVault = vault.copy(
            verifier = verifier,
        )

        vaultRepository.update(updatedVault)

        return Result.success(userKey)
    }

    fun reset() {
        currentVaultId.update { null }
    }

}

