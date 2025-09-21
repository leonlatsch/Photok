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
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64

private const val IV_SIZE = 16
private const val WRAPPED_USER_KEY = "wrapped_user_key"
private const val ANDROID_KEY_STORE = "AndroidKeyStore"
private const val WRAPPING_KEY_ALIAS = "user_key_wrapper"

/**
 * Provides secure storage of the user’s encryption key using Android’s KeyStore
 * with biometric authentication.
 *
 * Generates or retrieves a biometric-protected AES key, then wraps/unwraps the user key,
 * which is stored in [SharedPreferences].
 *
 * Main tasks:
 * - Create and access biometric-bound AES keys
 * - Provide ciphers for encrypting and decrypting the user key
 * - Persist and remove wrapped keys
 */
@Singleton
class BiometricKeyStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("biometric_keys", Context.MODE_PRIVATE)
    }

    fun userKeyExists(): Boolean {
        return prefs.getString(WRAPPED_USER_KEY, null).isNullOrEmpty().not()
    }

    fun removeStoredUserKey() {
        prefs.edit {
            remove(WRAPPED_USER_KEY)
            apply()
        }
    }

    fun getEncryptionCipher(): Result<Cipher> = runCatching {
        val key = getOrCreateSecretKey()

        Cipher.getInstance("AES/CBC/PKCS7Padding").apply {
            init(Cipher.ENCRYPT_MODE, key)
        }
    }


    fun getDecryptionCipher(): Result<Cipher> = runCatching {
        val blobBase64 = prefs.getString(WRAPPED_USER_KEY, null)
            ?: error("User key not stored")

        val wrapped = Base64.Default.decode(blobBase64)

        val key = getOrCreateSecretKey()
        val iv = wrapped.copyOfRange(0, IV_SIZE)

        Cipher.getInstance("AES/CBC/PKCS7Padding").apply {
            init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        }
    }

    /**
     * Encrypts the user key with the given [unlockedCipher].
     * [unlockedCipher] has to be a cipher that has been unlocked with biometric authentication.
     */
    fun encryptUserKey(userKey: SecretKey, unlockedCipher: Cipher): Result<Unit> = runCatching {
        val wrapped = unlockedCipher.doFinal(userKey.encoded)

        val finalBytes = unlockedCipher.iv + wrapped

        prefs.edit {
            putString(WRAPPED_USER_KEY, Base64.Default.encode(finalBytes))
            apply()
        }
    }

    /**
     * Decrypts the user key with the given [unlockedCipher].
     * [unlockedCipher] has to be a cipher that has been unlocked with biometric authentication.
     */
    fun decryptUserKey(unlockedCipher: Cipher): Result<SecretKey> = runCatching {
        val blobBase64 = prefs.getString(WRAPPED_USER_KEY, null)
            ?: error("User key not stored")

        val wrapped = Base64.Default.decode(blobBase64)
        val cipherText = wrapped.copyOfRange(IV_SIZE, wrapped.size)

        val keyBytes = unlockedCipher.doFinal(cipherText)
        SecretKeySpec(keyBytes, AES)
    }


    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        keyStore.getKey(WRAPPING_KEY_ALIAS, null)?.let { return it as SecretKey }

        val keyGenParams = KeyGenParameterSpec.Builder(
            WRAPPING_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
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
    private fun loadAndroidKeyStore(): KeyStore {
        return KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
    }
}
