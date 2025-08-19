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

import dev.leonlatsch.photok.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject


private const val ENC_VERSION_BYTE: Byte = 0x01
private const val IV_SIZE = 16
private const val SALT_SIZE = 16
private const val KEY_SIZE = 256
private const val ITERATION_COUNT = 100_000
private const val FULL_ALGORITHM = "PBKDF2WithHmacSHA256"
private const val ALGORITHM = "AES"

// FORMAT: [ENC_VERSION_BYTE][SALT][IV][ENCRYPTED_DATA]


class EncryptionManagerImpl @Inject constructor() : EncryptionManager {

    private val state: MutableStateFlow<State> =
        MutableStateFlow(State.Initial)

    override val isReady: Boolean
        get() = state.value is State.Ready

    override fun initialize(password: String) {
        if (password.length < 6) {
            state.update { State.Error }
            return
        }
        try {
            state.update {
                State.Ready(password = password)
            }
        } catch (e: GeneralSecurityException) {
            Timber.d("Error initializing EncryptionManager: $e")
            state.update { State.Error }
        }
    }

    override fun reset() {
        state.update { State.Initial }
    }

    override fun createCipherInputStream(
        input: InputStream,
        password: String?,
    ): CipherInputStream? {
        val passwordToUse = requirePassword(password)

        try {
            val version = input.read().toByte()
            if (version != ENC_VERSION_BYTE) throw IllegalArgumentException("Unsupported version")

            val salt = ByteArray(SALT_SIZE)
            val iv = ByteArray(IV_SIZE)

            input.read(salt, 0, salt.size)
            input.read(iv, 0, iv.size)

            val key = deriveAesKey(passwordToUse, salt)
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))

            return CipherInputStream(input, cipher)
        } catch (e: Exception) {
            Timber.d("Error creating CipherInputStream: $e")
            if (BuildConfig.DEBUG) throw RuntimeException(e)
            return null
        }
    }

    override fun createCipherOutputStream(
        output: OutputStream,
        password: String?,
    ): CipherOutputStream? {
        val passwordToUse = requirePassword(password)

        try {
            val salt = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }
            val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }

            val key = deriveAesKey(passwordToUse, salt)
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))

            output.write(byteArrayOf(ENC_VERSION_BYTE))
            output.write(salt)
            output.write(iv)

            return CipherOutputStream(output, cipher)
        } catch (e: Exception) {
            Timber.d("Error creating CipherOutputStream: $e")
            return null
        }
    }

    private fun requirePassword(override: String?): String {
        return override ?: (state.value as? State.Ready)?.password ?: error("EncryptionManager not initialized")
    }

    private fun deriveAesKey(password: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance(FULL_ALGORITHM)
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_SIZE)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, ALGORITHM)
    }

    private sealed interface State {
        data object Initial : State
        data object Error : State

        data class Ready(val password: String) : State
    }
}