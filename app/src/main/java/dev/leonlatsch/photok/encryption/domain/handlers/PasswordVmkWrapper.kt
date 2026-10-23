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
import dev.leonlatsch.photok.encryption.domain.models.Kdf
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionParams
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import kotlin.io.encoding.Base64

private const val KEK_SIZE = 256
const val PASSWORD_KEK_ITERATIONS = 300_000

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

        val params = VaultProtectionParams(
            salt = Base64.encode(salt),
            iv = Base64.encode(iv),
            kdf = Kdf.PBKDF2WithHmacSHA256,
            kdfIterations = PASSWORD_KEK_ITERATIONS,
            algorithm = Algorithm.AesCbcPkcs7Padding,
            keySize = KEK_SIZE,
        )

        val kek = keyGen.derivePasswordKeyEncryptionKey(
            password = password,
            salt = salt,
            kdf = Kdf.PBKDF2WithHmacSHA256,
            kdfIterations = PASSWORD_KEK_ITERATIONS,
            keySize = KEK_SIZE,
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
        return params.kdf != Kdf.PBKDF2WithHmacSHA256
            || (params.kdfIterations ?: 0) < PASSWORD_KEK_ITERATIONS
            || params.keySize != KEK_SIZE
            || params.algorithm != Algorithm.AesCbcPkcs7Padding
    }
}
