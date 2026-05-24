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

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.core.content.edit
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.crypto.IV_SIZE
import dev.leonlatsch.photok.encryption.domain.models.Algorithm
import dev.leonlatsch.photok.encryption.domain.models.CreateRequest
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import dev.leonlatsch.photok.encryption.domain.models.VaultProtection
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionParams
import dev.leonlatsch.photok.encryption.ui.UnlockBiometricCipherPrompt
import java.security.KeyStore
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
    private val app: Application,
    private val resources: Resources,
    private val unlockCipher: UnlockBiometricCipherPrompt,
) : VaultProtectionHandler<UnlockRequest.Biometric, CreateRequest.Biometric> {

    private val prefs = app.getSharedPreferences("biometric_keys", Context.MODE_PRIVATE)

    override suspend fun unlock(
        request: UnlockRequest.Biometric,
        protection: VaultProtection
    ): SecretKey {
        val kek = getOrCreateBiometricKek(
            algorithm = protection.params.algorithm,
            keySize = protection.params.keySize,
        )

        val iv = Base64.decode(protection.params.iv)

        val cipher = Cipher.getInstance(protection.params.algorithm.value).apply {
            init(Cipher.DECRYPT_MODE, kek, IvParameterSpec(iv))
        }

        val unlockedCipher = unlockCipher(
            fragment = request.fragment,
            cipher = cipher,
            title = resources.getString(R.string.biometric_unlock_title),
            subtitle = resources.getString(R.string.biometric_unlock_subtitle),
            negativeButtonText = resources.getString(R.string.biometric_unlock_cancel),
        ).getOrThrow()

        val vmkBytes = unlockedCipher.doFinal(protection.wrappedVMK)
        return SecretKeySpec(vmkBytes, "AES")
    }

    override suspend fun create(request: CreateRequest.Biometric): VaultProtection {
        val vmk = request.session.vmk

        val algorithm = Algorithm.AesCbcPkcs7Padding
        val keySize = 256

        val kek = getOrCreateBiometricKek(
            algorithm = algorithm,
            keySize = keySize,
        )

        val cipher = Cipher.getInstance(Algorithm.AesCbcPkcs7Padding.value).apply {
            init(Cipher.ENCRYPT_MODE, kek)
        }

        val unlockedCipher = unlockCipher(
            fragment = request.fragment,
            cipher = cipher,
            title = resources.getString(R.string.biometric_unlock_setup_title),
            subtitle = resources.getString(R.string.biometric_unlock_setup_subtitle),
            negativeButtonText = resources.getString(R.string.common_cancel),
        ).getOrThrow()

        val params = VaultProtectionParams(
            salt = null,
            iv = Base64.encode(unlockedCipher.iv),
            kdf = null,
            kdfIterations = null,
            algorithm = Algorithm.AesCbcPkcs7Padding,
            keySize = 256,
        )

        val wrappedVmk = unlockedCipher.doFinal(vmk.encoded)

        return VaultProtection(
            id = UUID.randomUUID().toString(),
            type = request.protectionType,
            wrappedVMK = wrappedVmk,
            params = params,
        )
    }

    override suspend fun canMigrate(): Boolean {
        return prefs.contains("wrapped_user_key")
    }

    override suspend fun migrate(request: UnlockRequest.Biometric): VaultProtection {
        val base64 = prefs.getString("wrapped_user_key", null)!!
        val bytes = Base64.decode(base64)

        val iv = bytes.copyOfRange(0, IV_SIZE)
        val wrappedVmk = bytes.copyOfRange(IV_SIZE, bytes.size)

        val params = VaultProtectionParams(
            salt = null,
            iv = Base64.encode(iv),
            kdf = null,
            kdfIterations = null,
            algorithm = Algorithm.AesCbcPkcs7Padding,
            keySize = 256,
        )

        return VaultProtection(
            id = UUID.randomUUID().toString(),
            type = request.protectionType,
            wrappedVMK = wrappedVmk,
            params = params,
        )
    }

    override suspend fun onMigrationPersisted() {
        prefs.edit { remove("wrapped_user_key") }
    }

    override suspend fun reset() {
        getKeyStore().deleteEntry(WRAPPING_KEY_ALIAS)
    }
}

private fun getOrCreateBiometricKek(
    algorithm: Algorithm,
    keySize: Int,
): SecretKey {
    val keyStore = getKeyStore()
    keyStore.getKey(WRAPPING_KEY_ALIAS, null)?.let { return it as SecretKey }

    val keyGenParams = KeyGenParameterSpec.Builder(
        WRAPPING_KEY_ALIAS,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(algorithm.blockMode)
        .setEncryptionPaddings(algorithm.padding)
        .setUserAuthenticationRequired(true)
        .setKeySize(keySize)
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
