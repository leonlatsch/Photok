/*
 *   Copyright 2020-2024 Leon Latsch
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

package dev.leonlatsch.photok.security.biometric

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.security.AES
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64

/**
 * Marks a method as protected by biometric authentication.
 */
annotation class ProtectedByBiometric

private const val ENCRYPTED_USER_KEY = "user_key_bio_protected"
private const val PROTECTION_KEY_ALIAS = "bio_protection_key"
private const val PROTECTION_KEY_ALGORITHM = "AES/GCM/NoPadding"
private const val ANDROID_KEY_STORE = "AndroidKeyStore"

@Singleton
class BiometricKeyStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("biometric_keys", Context.MODE_PRIVATE)
    }

    fun removeStoredUserKey() {
        prefs.edit {
            remove(ENCRYPTED_USER_KEY)
            apply()
        }
    }

    @ProtectedByBiometric
    fun storeUserKey(key: SecretKey): Result<Unit> = runCatching {
        createProtectionKeyIfNeeded()

        val keyStore = loadAndroidKeyStore()
        val protectionKey = keyStore.getKey(PROTECTION_KEY_ALIAS, null) as SecretKey

        val cipher = Cipher.getInstance(PROTECTION_KEY_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, protectionKey)
        val iv = cipher.iv
        val wrapped = cipher.doFinal(key.encoded)

        val blob = iv + wrapped

        prefs.edit {
            putString(ENCRYPTED_USER_KEY, Base64.encode(blob))
            apply()
        }
    }

    @ProtectedByBiometric
    fun getUserKey(): Result<SecretKey> = runCatching {
        val blobBase64 = prefs.getString(ENCRYPTED_USER_KEY, null) ?: error("User key not stored")

        val blob = Base64.decode(blobBase64)
        val iv = blob.copyOfRange(0, 12)
        val key = blob.copyOfRange(12, blob.size)

        val keyStore = loadAndroidKeyStore()
        val protectionKey = keyStore.getKey(PROTECTION_KEY_ALIAS, null) as SecretKey

        val cipher = Cipher.getInstance(PROTECTION_KEY_ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, protectionKey, GCMParameterSpec(128, iv))
        val keyBytes = cipher.doFinal(key)

        SecretKeySpec(keyBytes, AES)
    }

    @ProtectedByBiometric
    private fun createProtectionKeyIfNeeded() {
        val keyStore = loadAndroidKeyStore()
        if (!keyStore.containsAlias(PROTECTION_KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEY_STORE
            )
            val spec = KeyGenParameterSpec.Builder(
                PROTECTION_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(true) // Requires biometric authentication
                .build()

            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }
    }

    private fun loadAndroidKeyStore(): KeyStore {
        return KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
    }

}