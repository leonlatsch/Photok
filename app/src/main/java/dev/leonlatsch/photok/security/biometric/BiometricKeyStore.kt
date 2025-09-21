package dev.leonlatsch.photok.security.biometric

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyPairGenerator
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64

/**
 * Marks a method as protected by biometric authentication.
 */
annotation class ProtectedByBiometric

private const val WRAPPED_USER_KEY = "wrapped_user_key"
private const val RSA_ALIAS = "bio_protection_rsa"
private const val ANDROID_KEY_STORE = "AndroidKeyStore"
private const val RSA_ALGORITHM = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"

@Singleton
class BiometricKeyStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("biometric_keys", Context.MODE_PRIVATE)
    }

    fun removeStoredUserKey() {
        prefs.edit {
            remove(WRAPPED_USER_KEY)
            apply()
        }

        loadAndroidKeyStore().deleteEntry(RSA_ALIAS)
    }

    @ProtectedByBiometric
    fun storeUserKey(userKey: SecretKey): Result<Unit> = runCatching {
        createRsaKeyIfNeeded()
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        val publicKey = keyStore.getCertificate(RSA_ALIAS).publicKey

        val cipher = Cipher.getInstance(RSA_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val wrapped = cipher.doFinal(userKey.encoded)

        prefs.edit {
            putString(WRAPPED_USER_KEY, Base64.encode(wrapped))
            apply()
        }
    }

    @ProtectedByBiometric
    fun getUserKey(): Result<SecretKey> = runCatching {
        val blobBase64 = prefs.getString(WRAPPED_USER_KEY, null)
            ?: error("User key not stored")

        val wrapped = Base64.Default.decode(blobBase64)

        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        val privateKey = keyStore.getKey(RSA_ALIAS, null)

        val cipher = Cipher.getInstance(RSA_ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)

        val keyBytes = cipher.doFinal(wrapped)
        SecretKeySpec(keyBytes, "AES")
    }

    @ProtectedByBiometric
    private fun createRsaKeyIfNeeded() {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        if (!keyStore.containsAlias(RSA_ALIAS)) {
            val kpg = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA,
                ANDROID_KEY_STORE
            )
            val spec = KeyGenParameterSpec.Builder(
                RSA_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                .setUserAuthenticationRequired(true)
                .build()
            kpg.initialize(spec)
            kpg.generateKeyPair()
        }
    }
    private fun loadAndroidKeyStore(): KeyStore {
        return KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
    }
}
