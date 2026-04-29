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

package dev.leonlatsch.photok.encryption.domain.unlockers

import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import dev.leonlatsch.photok.encryption.domain.models.VaultProtection
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionParams
import dev.leonlatsch.photok.security.AES
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import kotlin.io.encoding.Base64

class PasswordProtectionUnlocker @Inject constructor() : ProtectionUnlocker<UnlockRequest.Password> {

    override suspend fun unlock(
        request: UnlockRequest.Password,
        protection: VaultProtection
    ): SecretKey {
        val keyEncryptionKey = deriveAesKey(request.password, protection.params)

        val cipher = Cipher.getInstance(protection.params.algorithm.value).apply {
            val iv = Base64.decode(protection.params.iv)
            init(Cipher.DECRYPT_MODE, keyEncryptionKey, IvParameterSpec(iv))
        }

        val vmkBytes = cipher.doFinal(protection.wrappedVMK)
        return SecretKeySpec(vmkBytes, AES)
    }

    private fun deriveAesKey(password: String, params: VaultProtectionParams): SecretKey {
        val salt = Base64.decode(params.salt)

        val factory = SecretKeyFactory.getInstance(params.kdf.value)
        val spec = PBEKeySpec(password.toCharArray(), salt, params.kdfIterations, params.keySize)
        val keyBytes = factory.generateSecret(spec).encoded

        return SecretKeySpec(keyBytes, AES)
    }
}