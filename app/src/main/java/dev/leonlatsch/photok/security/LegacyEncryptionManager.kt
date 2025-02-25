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

import dev.leonlatsch.photok.other.AES
import dev.leonlatsch.photok.other.AES_ALGORITHM
import dev.leonlatsch.photok.other.SHA_256
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

/**
 * Manages encryption.
 * Holds the AES key and serves it to the app's components.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class LegacyEncryptionManager @Inject constructor() : EncryptionManager {

    private var encryptionKey: SecretKeySpec? = null
    private var ivParameterSpec: IvParameterSpec? = null

    override var isReady: Boolean = false

    val encodedKey: ByteArray
        get() = encryptionKey!!.encoded

    /**
     * Initialize the [SecretKeySpec] with a [password].
     * Uses the [password] to create a SHA-256 hash binary that is used to create [SecretKeySpec].
     * Generates [IvParameterSpec] for GCM.
     *
     * @param password the password string to use.
     */
    override fun initialize(password: String) {
        if (password.length < 6) {
            isReady = false
            return
        }
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
    override fun reset() {
        encryptionKey = null
        ivParameterSpec = null
        isReady = false
    }

    /**
     * Turn a [InputStream] into an [CipherInputStream] with the stored [encryptionKey] or
     * an encryption key generated from the [password] if given.
     *
     * @param password if not null, this will be used for decrypting
     */
    override fun createCipherInputStream(
        inputStream: InputStream,
        password: String?
    ): CipherInputStream? {
        return if (isReady) try {
            val cipher = if (password == null) {
                createCipher(Cipher.DECRYPT_MODE)
            } else {
                createCipher(Cipher.DECRYPT_MODE, password)
            }

            CipherInputStream(inputStream, cipher)
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
    override fun createCipherOutputStream(
        outputStream: OutputStream,
        password: String?
    ): CipherOutputStream? {
        return if (isReady) try {
            val cipher = if (password == null) {
                createCipher(Cipher.ENCRYPT_MODE)
            } else {
                createCipher(Cipher.ENCRYPT_MODE, password)
            }

            CipherOutputStream(outputStream, cipher)
        } catch (e: GeneralSecurityException) {
            Timber.d("Error creating encrypted output stream: $e")
            null
        } else {
            null
        }
    }

    private fun createCipher(mode: Int, password: String): Cipher? {
        val key = genSecKey(password)
        val iv = genIv(password)

        return createCipher(mode, key, iv)
    }

    /**
     * Create a cipher with local stored encryption key.
     */
    override fun createCipher(mode: Int) = createCipher(mode, encryptionKey, ivParameterSpec)

    private fun createCipher(
        mode: Int,
        secretKeySpec: SecretKeySpec?,
        ivParam: IvParameterSpec?
    ): Cipher? {
        return if (isReady) try {
            Cipher.getInstance(AES_ALGORITHM).apply {
                init(mode, secretKeySpec, ivParam)
            }
        } catch (e: GeneralSecurityException) {
            Timber.d("Error initializing cipher: $e")
            null
        } else {
            Timber.d("EncryptionManager has to be ready to create a cipher")
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
        val firstChars = charArray.take(16)
        for (i in firstChars.indices) {
            iv[i] = firstChars[i].toByte()
        }

        return IvParameterSpec(iv)
    }
}