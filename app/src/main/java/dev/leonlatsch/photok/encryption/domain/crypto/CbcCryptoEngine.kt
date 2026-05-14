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

package dev.leonlatsch.photok.encryption.domain.crypto

import dev.leonlatsch.photok.encryption.domain.models.Algorithm
import dev.leonlatsch.photok.encryption.domain.models.EncryptionVersionByte
import dev.leonlatsch.photok.security.SALT_SIZE
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

/**
 * Formats:
 *
 * 1: [ENC_VERSION_BYTE][SALT][IV][ENCRYPTED_DATA]
 * 2: [ENC_VERSION_BYTE]      [IV][ENCRYPTED_DATA]
 */
class CbcCryptoEngine @Inject constructor(): CryptoEngine {

    override fun createEncryptStream(output: OutputStream, key: SecretKey): CipherOutputStream? {
        try {
            val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }

            val cipher = Cipher.getInstance(Algorithm.AesCbcPkcs7Padding.value).apply {
                init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
            }

            output.write(byteArrayOf(EncryptionVersionByte.Two.value))
            output.write(iv)

            return CipherOutputStream(output, cipher)
        } catch (e: Exception) {
            Timber.e("Error creating CipherOutputStream: $e")
            return null
        }
    }

    override fun createDecryptStream(input: InputStream, key: SecretKey): CipherInputStream? {
        try  {
            val versionByte = input.read().toByte()
            val version = EncryptionVersionByte.fromValue(versionByte)

            val iv = ByteArray(IV_SIZE)

            when (version) {
                EncryptionVersionByte.One -> {
                    val salt = ByteArray(SALT_SIZE)
                    input.read(salt, 0, salt.size)
                    input.read(iv, 0, iv.size)
                }
                EncryptionVersionByte.Two -> {
                    input.read(iv, 0, iv.size)
                }
            }

            val cipher = Cipher.getInstance(Algorithm.AesCbcPkcs7Padding.value).apply {
                init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
            }

            return CipherInputStream(input, cipher)
        } catch (e: Exception) {
            Timber.e("Error creating CipherInputStream: $e")
            return null
        }
    }

}