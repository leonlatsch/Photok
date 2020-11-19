/*
 *   Copyright 2020 Leon Latsch
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

import dev.leonlatsch.photok.other.AES
import dev.leonlatsch.photok.other.AES_ALGORITHM
import dev.leonlatsch.photok.other.SHA_256
import timber.log.Timber
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.MessageDigest
import javax.crypto.Cipher
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

    private var encryptionKey: SecretKeySpec? = null
    private var ivParameterSpec: IvParameterSpec? = null

    var isReady: Boolean = false


    /**
     * Initialize the [SecretKeySpec] with a [password].
     * Uses the [password] to create a SHA-256 hash binary that is used to create [SecretKeySpec].
     * Generates [IvParameterSpec] for GCM.
     *
     * @param password the password string to use.
     */
    fun initialize(password: String) {
        try {
            encryptionKey = genSecKey(password)
            ivParameterSpec = genIv(password)
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
    }


    /**
     * Encrypt a [ByteArray] with the stored [SecretKeySpec].
     */
    fun encrypt(bytes: ByteArray): ByteArray? {
        return if (isReady) try {
            val cipher = Cipher.getInstance(AES_ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, ivParameterSpec)
            cipher.doFinal(bytes)
        } catch (e: GeneralSecurityException) {
            Timber.d("Error encrypting bytes: $e")
            null
        } else {
            null
        }
    }

    /**
     * Decrypt a [ByteArray] with the stored [SecretKeySpec]
     */
    fun decrypt(encryptedBytes: ByteArray): ByteArray? {
        return if (isReady) try {
            val cipher = Cipher.getInstance(AES_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, ivParameterSpec)
            cipher.doFinal(encryptedBytes)
        } catch (e: GeneralSecurityException) {
            Timber.d("Error decrypting bytes: $e")
            null
        } else {
            null
        }
    }

    /**
     * Encrypt [bytes] with a specific password.
     * USE WITH CAUTION!
     * Used by re-encrypt dialog.
     */
    fun encrypt(bytes: ByteArray, password: String): ByteArray? {
        return try {
            val key = genSecKey(password)
            val iv = genIv(password)

            val cipher = Cipher.getInstance(AES_ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, key, iv)

            cipher.doFinal(bytes)
        } catch (e: GeneralSecurityException) {
            Timber.d("Error encrypting bytes: $e")
            null
        }
    }

    /**
     * Encrypt [bytes] with a specific password.
     * USE WITH CAUTION!
     * Used by re-encrypt dialog.
     */
    fun decrypt(bytes: ByteArray, password: String): ByteArray? {
        return try {
            val key = genSecKey(password)
            val iv = genIv(password)

            val cipher = Cipher.getInstance(AES_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, key, iv)

            cipher.doFinal(bytes)
        } catch (e: GeneralSecurityException) {
            Timber.d("Error decrypting bytes: $e")
            null
        }
    }

    private fun genSecKey(password: String): SecretKeySpec {
        val md = MessageDigest.getInstance(SHA_256)
        val bytes = md.digest(password.toByteArray(StandardCharsets.UTF_8))
        return SecretKeySpec(bytes, AES)
    }

    private fun genIv(password: String): IvParameterSpec {
        val iv = ByteArray(16)
        val charArray = password.toCharArray()
        for (i in iv.indices) {
            iv[i] = charArray[i].toByte()
        }
        return IvParameterSpec(iv)
    }
}