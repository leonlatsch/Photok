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
import dev.leonlatsch.photok.encryption.domain.models.KekSpec
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionParams
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import kotlin.io.encoding.Base64

class WrappedVmk(
    val params: VaultProtectionParams,
    val wrappedVMK: ByteArray,
)

class PasswordVmkWrapper @Inject constructor(
    private val keyGen: KeyGen,
) {

    fun wrap(password: String, vmk: SecretKey): WrappedVmk {
        val salt = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }
        val spec = KekSpec.Password

        val params = VaultProtectionParams(
            salt = Base64.encode(salt),
            iv = Base64.encode(iv),
            kdf = spec.kdf,
            kdfIterations = spec.kdfIterations,
            algorithm = spec.algorithm,
            keySize = spec.keySize,
        )

        val kek = keyGen.derivePasswordKeyEncryptionKey(
            password = password,
            salt = salt,
            kdf = spec.kdf,
            kdfIterations = spec.kdfIterations,
            keySize = spec.keySize,
        )

        val cipher = Cipher.getInstance(params.algorithm.value).apply {
            init(Cipher.ENCRYPT_MODE, kek, IvParameterSpec(iv))
        }

        return WrappedVmk(
            params = params,
            wrappedVMK = cipher.doFinal(vmk.encoded),
        )
    }

    fun isOutdated(params: VaultProtectionParams): Boolean {
        return !KekSpec.Password.matches(params)
    }
}
