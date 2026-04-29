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

import dev.leonlatsch.photok.encryption.domain.models.Algorithm
import dev.leonlatsch.photok.encryption.domain.models.CreateRequest
import dev.leonlatsch.photok.encryption.domain.models.Kdf
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import dev.leonlatsch.photok.encryption.domain.models.VaultProtection
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionParams
import dev.leonlatsch.photok.security.AES
import dev.leonlatsch.photok.security.ITERATION_COUNT
import dev.leonlatsch.photok.security.IV_SIZE
import dev.leonlatsch.photok.security.KEY_SIZE
import dev.leonlatsch.photok.security.SALT_SIZE
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
import kotlin.io.encoding.Base64

class PasswordVaultProtectionHandler @Inject constructor() :
    VaultProtectionHandler<UnlockRequest.Password, CreateRequest.Password> {

    override suspend fun unlock(
        request: UnlockRequest.Password,
        protection: VaultProtection
    ): SecretKey {
        val kek = deriveKeyEncryptionKey(request.password, protection.params)

        val cipher = Cipher.getInstance(protection.params.algorithm.value).apply {
            val iv = Base64.decode(protection.params.iv)
            init(Cipher.DECRYPT_MODE, kek, IvParameterSpec(iv))
        }

        val vmkBytes = cipher.doFinal(protection.wrappedVMK)
        return SecretKeySpec(vmkBytes, AES)
    }

    override suspend fun create(request: CreateRequest.Password): VaultProtection {

        val salt = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }

        val params = VaultProtectionParams(
            salt = Base64.encode(salt),
            iv = Base64.encode(iv),
            kdf = Kdf.PBKDF2WithHmacSHA256,
            kdfIterations = ITERATION_COUNT,
            algorithm = Algorithm.AesCbcPkcs7Padding,
            keySize = KEY_SIZE,
        )

        val vmk = generateVaultMasterKey(params)

        val kek = deriveKeyEncryptionKey(request.password, params)

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

    private fun deriveKeyEncryptionKey(password: String, params: VaultProtectionParams): SecretKey {
        requireNotNull(params.salt)
        requireNotNull(params.kdf)
        requireNotNull(params.kdfIterations)

        val salt = Base64.decode(params.salt)

        val factory = SecretKeyFactory.getInstance(params.kdf.value)
        val spec = PBEKeySpec(password.toCharArray(), salt, params.kdfIterations, params.keySize)
        val keyBytes = factory.generateSecret(spec).encoded

        return SecretKeySpec(keyBytes, AES)
    }

    private fun generateVaultMasterKey(params: VaultProtectionParams): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(Algorithm.AesCbcPkcs7Padding.value).apply {
            init(params.keySize)
        }

        return keyGenerator.generateKey()
    }
}