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

package dev.leonlatsch.photok.security

import jakarta.inject.Inject
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import java.lang.annotation.ElementType
import java.lang.annotation.Target
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Qualifier
import javax.inject.Singleton

private const val SHA_256 = "SHA-256"
private const val AES = "AES"
private const val AES_ALGORITHM = "AES/GCM/NoPadding"

@Singleton
class LegacyEncryptionManagerImpl @Inject constructor(
) : EncryptionManager {

    private var key: SecretKeySpec? = null
    private var iv: IvParameterSpec? = null

    override var isReady: Boolean = false

    override fun initialize(password: String) {
        if (password.length < 6) {
            isReady = false
            return
        }

        try {
            key = genSecKey(password)
            iv = genLegacyIv(password)
            isReady = true
        } catch (e: GeneralSecurityException) {
            Timber.d("Error initializing EncryptionManager: $e")
            reset()
        }
    }

    override fun reset() {
        key = null
        iv = null
        isReady = false
    }

    override fun createCipherInputStream(
        input: InputStream,
        fileName: String?,
        password: String?
    ): CipherInputStream? {
        val key: SecretKeySpec
        val iv: IvParameterSpec

        if (password != null) {
            key = genSecKey(password)
            iv = genLegacyIv(password)
        } else {
            key = this.key ?: return null
            iv = this.iv ?: return null
        }

        val cipher = Cipher.getInstance(AES_ALGORITHM).apply {
            init(Cipher.DECRYPT_MODE, key, iv)
        }

        return CipherInputStream(input, cipher)
    }

    override fun createCipherOutputStream(
        output: OutputStream,
        fileName: String?,
        password: String?
    ): CipherOutputStream? {
        val key: SecretKeySpec
        val iv: IvParameterSpec

        if (password != null) {
            key = genSecKey(password)
            iv = genLegacyIv(password)
        } else {
            key = this.key ?: return null
            iv = this.iv ?: return null
        }

        val cipher = Cipher.getInstance(AES_ALGORITHM).apply {
            init(Cipher.ENCRYPT_MODE, key, iv)
        }

        return CipherOutputStream(output, cipher)
    }

    private fun genSecKey(password: String): SecretKeySpec {
        val md = MessageDigest.getInstance(SHA_256)
        val bytes = md.digest(password.toByteArray(StandardCharsets.UTF_8))
        return SecretKeySpec(bytes, AES)
    }

    private fun genLegacyIv(password: String): IvParameterSpec {
        val iv = ByteArray(16)
        val charArray = password.toCharArray()
        val firstChars = charArray.take(16)
        for (i in firstChars.indices) {
            iv[i] = firstChars[i].toByte()
        }

        return IvParameterSpec(iv)
    }
}