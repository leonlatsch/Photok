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
import dev.leonlatsch.photok.encryption.domain.models.Session
import dev.leonlatsch.photok.encryption.domain.models.VaultSession
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

class CbcCryptoEngine @Inject constructor(): CryptoEngine {

    override fun createEncryptStream(output: OutputStream, session: Session): CipherOutputStream? {
        require(session is VaultSession)

        try {
            val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }

            val cipher = Cipher.getInstance(Algorithm.AesCbcPkcs7Padding.value).apply {
                init(Cipher.ENCRYPT_MODE, session.vmk, IvParameterSpec(iv))
            }

            output.write(byteArrayOf(EncryptionVersionByte.Two.value))
            output.write(iv)

            return CipherOutputStream(output, cipher)
        } catch (e: Exception) {
            Timber.e("Error creating CipherOutputStream: $e")
            return null
        }
    }

    override fun createDecryptStream(input: InputStream, session: Session): CipherInputStream? {
        require(session is VaultSession)

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
                init(Cipher.DECRYPT_MODE, session.vmk, IvParameterSpec(iv))
            }

            return CipherInputStream(input, cipher)
        } catch (e: Exception) {
            Timber.e("Error creating CipherInputStream: $e")
            return null
        }
    }

}