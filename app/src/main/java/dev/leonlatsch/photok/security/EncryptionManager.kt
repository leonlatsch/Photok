/*
 *   Copyright 2020-2022 Leon Latsch
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

package dev.leonlatsch.photok.security

import android.os.Build
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import androidx.annotation.RequiresApi
import dev.leonlatsch.photok.other.*
import org.gradle.internal.impldep.com.amazonaws.services.s3.internal.crypto.CryptoUtils
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Manages encryption.
 * Holds the AES key and serves it to the app's components.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class EncryptionManager {

    private var encryptionKey: SecretKey? = null
    private var ivParameterSpec: IvParameterSpec? = null

    var useAndroidKeyStore = false
    var isReady: Boolean = false

    val encodedKey: ByteArray
        get() = encryptionKey!!.encoded

    /**
     * Initialize the [SecretKeySpec] with a [password].
     * Uses the [password] to create a SHA-256 hash binary that is used to create [SecretKeySpec].
     * Generates [IvParameterSpec] for GCM.
     *
     * @param password the password string to use.
     */
    fun initialize(password: String) {
        if (password.length < 6) {
            isReady = false
            return
        }
        try {
            encryptionKey = genSecKey(password)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                throw GeneralSecurityException("Android KeyStore is only supported on Android 8+")
            }
            ivParameterSpec = getIVSecureRandom(AES_ALGORITHM)
            isReady = true
        } catch (e: GeneralSecurityException) {
            Timber.d("Error initializing EncryptionManager: $e")
            isReady = false
        }
    }

    fun initializeWithAndroidKeyStore() {
        useAndroidKeyStore = true
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                throw GeneralSecurityException("Android KeyStore is only supported on Android 9+")
            }
            encryptionKey = getSecKeyFromAndroidKeyStore()
            ivParameterSpec = getIVSecureRandom(AES_ALGORITHM)
            isReady = true
        } catch (e: GeneralSecurityException) {
            Timber.d("Error initializing EncryptionManager: $e")
            isReady = false
        }
    }

    /**
     * Resets the encryption manager to default state.
     */
    fun reset() {
        encryptionKey = null
        ivParameterSpec = null
        isReady = false
        useAndroidKeyStore = false
    }

    /**
     * Turn a [InputStream] into an [CipherInputStream] with the stored [encryptionKey] or
     * an encryption key generated from the [password] if given.
     *
     * @param password if not null, this will be used for decrypting
     */
    fun createCipherInputStream(
        origInputStream: InputStream,
        password: String? = null
    ): CipherInputStream? {
        return if (isReady) try {
            val cipher = if (password == null) {
                createCipher(Cipher.DECRYPT_MODE)
            } else {
                createCipher(Cipher.DECRYPT_MODE, password)
            }

            CipherInputStream(origInputStream, cipher)
        } catch (e: GeneralSecurityException) {
            Timber.d("Error creating encrypted input stream: $e")
            null
        } else {
            null
        }
    }

    /**
     * Turn a [OutputStream] into an [CipherOutputStream] with the stored [encryptionKey] or
     * an encryption key generated from the [password] if given.
     *
     * @param password if not null, this will be used for encrypting
     */
    fun createCipherOutputStream(
        origOutputStream: OutputStream,
        password: String? = null
    ): CipherOutputStream? {
        return if (isReady) try {
            val cipher = if (password == null) {
                createCipher(Cipher.ENCRYPT_MODE)
            } else {
                createCipher(Cipher.ENCRYPT_MODE, password)
            }

            CipherOutputStream(origOutputStream, cipher)
        } catch (e: GeneralSecurityException) {
            Timber.d("Error creating encrypted output stream: $e")
            null
        } else {
            null
        }
    }

    private fun createCipher(mode: Int, password: String): Cipher? {
        val key = genSecKey(password)
        val iv = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getIVSecureRandom(AES_ALGORITHM)
        } else {
            TODO("VERSION.SDK_INT < O")
        }

        return createCipher(mode, key, iv)
    }

    /**
     * Create a cipher with local stored encryption key.
     */
    private fun createCipher(mode: Int) = createCipher(mode, encryptionKey, ivParameterSpec)

    private fun createCipher(
        mode: Int,
        secretKeySpec: SecretKey?,
        ivParam: IvParameterSpec?
    ): Cipher? {
        return if (isReady) try {
            Cipher.getInstance(AES_ALGORITHM).apply {
                init(mode, secretKeySpec)
            }
        } catch (e: GeneralSecurityException) {
            Timber.d("Error initializing cipher: $e")
            null
        } else {
            Timber.d("EncryptionManager has to be ready to create a cipher")
            null
        }
    }

    private fun genSecKey(password: String): SecretKey {
        val md = MessageDigest.getInstance(SHA_256)
        val bytes = md.digest(password.toByteArray(StandardCharsets.UTF_8))
        return SecretKeySpec(bytes, AES)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun getSecKeyFromAndroidKeyStore(): SecretKey {
        val keyStore = KeyStore.getInstance(KEY_STORE).apply { load(null) }
        if (!keyStore.containsAlias(KEY_STORE_KEY_ALIAS)) {
            throw Exception("No private key exists in the android key-store!")
        }

        val entry = keyStore.getKey(KEY_STORE_KEY_ALIAS, null) as KeyStore.SecretKeyEntry
        return entry.secretKey
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun importSecKeyIntoAndroidKeyStore() {
        val keyStore = KeyStore.getInstance(KEY_STORE).apply { load(null) }

        keyStore.setEntry(
            KEY_STORE_KEY_ALIAS,
            KeyStore.SecretKeyEntry(encryptionKey),
            KeyProtection.Builder(KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationParameters(
                    0,
                    KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL
                )
                .build()
        )
    }

    /*
    private fun genIv(password: String): IvParameterSpec {
        val iv = ByteArray(16)
        val charArray = password.toCharArray()
        val firstChars = charArray.take(16)
        for (i in firstChars.indices) {
            iv[i] = firstChars[i].toByte()
        }

        return IvParameterSpec(iv)
    }
    */

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getIVSecureRandom(algorithm: String?): IvParameterSpec? {
        val iv =
            CryptoUtils.getRandomIVWithSize(
                12
            )
        return GCMParameterSpec(128, iv)
    }
}