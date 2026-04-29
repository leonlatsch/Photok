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

import android.content.res.Resources
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import dev.leonlatsch.photok.encryption.domain.models.VaultProtection
import dev.leonlatsch.photok.security.AES
import dev.leonlatsch.photok.security.biometric.UnlockCipherUseCase
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import kotlin.io.encoding.Base64

private const val IV_SIZE = 16
private const val WRAPPED_USER_KEY = "wrapped_user_key"
private const val ANDROID_KEY_STORE = "AndroidKeyStore"
private const val WRAPPING_KEY_ALIAS = "user_key_wrapper"

class BiometricProtectionUnlocker @Inject constructor(
    private val resources: Resources,
    private val unlockCipher: UnlockCipherUseCase,
) : ProtectionUnlocker<UnlockRequest.Biometric> {

    override suspend fun unlock(
        request: UnlockRequest.Biometric,
        protection: VaultProtection
    ): SecretKey {
        val key = getOrCreateSecretKey()

        val cipher = Cipher.getInstance(protection.params.algorithm.value).apply {
            val iv = Base64.decode(protection.params.iv)

            init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        }

        val unlockedCipher = unlockCipher(
            request.fragment,
            cipher = cipher,
            title = resources.getString(R.string.biometric_unlock_setup_title),
            subtitle = resources.getString(R.string.biometric_unlock_setup_subtitle),
            negativeButtonText = resources.getString(R.string.common_cancel),
        ).getOrThrow()

        val vmkBytes = unlockedCipher.doFinal(protection.wrappedVMK)
        return SecretKeySpec(vmkBytes, AES)
    }

}

private fun getOrCreateSecretKey(): SecretKey {
    val keyStore = getKeyStore()
    keyStore.getKey(WRAPPING_KEY_ALIAS, null)?.let { return it as SecretKey }

    val keyGenParams = KeyGenParameterSpec.Builder(
        WRAPPING_KEY_ALIAS,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
        .setUserAuthenticationRequired(true)
        .setKeySize(256)
        .setInvalidatedByBiometricEnrollment(true)
        .build()

    val keyGenerator = KeyGenerator.getInstance(
        KeyProperties.KEY_ALGORITHM_AES,
        ANDROID_KEY_STORE
    )
    keyGenerator.init(keyGenParams)
    return keyGenerator.generateKey()
}

private fun getKeyStore(): KeyStore {
    return KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
}
