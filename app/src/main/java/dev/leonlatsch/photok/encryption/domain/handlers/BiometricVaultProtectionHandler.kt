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

import android.content.res.Resources
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.models.Algorithm
import dev.leonlatsch.photok.encryption.domain.models.CreateRequest
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import dev.leonlatsch.photok.encryption.domain.models.VaultProtection
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionParams
import dev.leonlatsch.photok.security.AES
import dev.leonlatsch.photok.security.IV_SIZE
import dev.leonlatsch.photok.security.KEY_SIZE
import dev.leonlatsch.photok.security.biometric.UnlockCipherUseCase
import java.security.KeyStore
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import kotlin.io.encoding.Base64

private const val ANDROID_KEY_STORE = "AndroidKeyStore"
private const val WRAPPING_KEY_ALIAS = "user_key_wrapper"

class BiometricVaultProtectionHandler @Inject constructor(
    private val resources: Resources,
    private val unlockCipher: UnlockCipherUseCase,
) : VaultProtectionHandler<UnlockRequest.Biometric, CreateRequest.Biometric> {

    override suspend fun unlock(
        request: UnlockRequest.Biometric,
        protection: VaultProtection
    ): SecretKey {
        val kek = getOrCreateSecretKey(protection.params)

        val cipher = Cipher.getInstance(protection.params.algorithm.value).apply {
            val iv = Base64.decode(protection.params.iv)

            init(Cipher.DECRYPT_MODE, kek, IvParameterSpec(iv))
        }

        val unlockedCipher = unlockCipher(
            fragment = request.fragment,
            cipher = cipher,
            title = resources.getString(R.string.biometric_unlock_setup_title),
            subtitle = resources.getString(R.string.biometric_unlock_setup_subtitle),
            negativeButtonText = resources.getString(R.string.common_cancel),
        ).getOrThrow()

        val vmkBytes = unlockedCipher.doFinal(protection.wrappedVMK)
        return SecretKeySpec(vmkBytes, AES)
    }

    override suspend fun create(request: CreateRequest.Biometric): VaultProtection {
        val vmk = request.session.vmk
        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }

        val params = VaultProtectionParams(
            salt = null,
            iv = Base64.encode(iv),
            kdf = null,
            kdfIterations = null,
            algorithm = Algorithm.AesCbcPkcs7Padding,
            keySize = KEY_SIZE,
        )

        val kek = getOrCreateSecretKey(params)

        val cipher = Cipher.getInstance(Algorithm.AesCbcPkcs7Padding.value).apply {
            init(Cipher.ENCRYPT_MODE, kek, IvParameterSpec(iv))
        }


        val unlockedCipher = unlockCipher(
            fragment = request.fragment,
            cipher = cipher,
            title = resources.getString(R.string.biometric_unlock_setup_title),
            subtitle = resources.getString(R.string.biometric_unlock_setup_subtitle),
            negativeButtonText = resources.getString(R.string.common_cancel),
        ).getOrThrow()

        val wrappedVmk = unlockedCipher.doFinal(vmk.encoded)

        return VaultProtection(
            id = UUID.randomUUID().toString(),
            type = request.protectionType,
            wrappedVMK = wrappedVmk,
            params = params,
        )
    }

}

private fun getOrCreateSecretKey(params: VaultProtectionParams): SecretKey {
    val keyStore = getKeyStore()
    keyStore.getKey(WRAPPING_KEY_ALIAS, null)?.let { return it as SecretKey }

    val keyGenParams = KeyGenParameterSpec.Builder(
        WRAPPING_KEY_ALIAS,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(params.algorithm.blockMode)
        .setEncryptionPaddings(params.algorithm.padding)
        .setUserAuthenticationRequired(true)
        .setKeySize(params.keySize)
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
