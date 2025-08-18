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

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
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
import kotlin.coroutines.suspendCoroutine

sealed interface EncryptionManagerState {
    data object Initial : EncryptionManagerState
    data object Error : EncryptionManagerState

    data class Ready(val password: String) : EncryptionManagerState
}

private const val ENC_VERSION_BYTE: Byte = 0x01
private const val IV_SIZE = 16
private const val SALT_SIZE = 16
private const val KEY_SIZE = 256
private const val ITERATION_COUNT = 100_000
private const val FULL_ALGORITHM = "PBKDF2WithHmacSHA256"
private const val ALGORITHM = "AES"

// FORMAT: [ENC_VERSION_BYTE][SALT][IV][ENCRYPTED_DATA]


class EncryptionManagerImpl @Inject constructor() : EncryptionManager {

    private val state: MutableStateFlow<EncryptionManagerState> =
        MutableStateFlow(EncryptionManagerState.Initial)

    override val isReady: Boolean
        get() = state.value is EncryptionManagerState.Ready

    override fun initialize(password: String) {
        if (password.length < 6) {
            state.update { EncryptionManagerState.Error }
            return
        }
        try {
            state.update {
                EncryptionManagerState.Ready(password = password)
            }
        } catch (e: GeneralSecurityException) {
            Timber.d("Error initializing EncryptionManager: $e")
            state.update { EncryptionManagerState.Error }
        }
    }

    override fun reset() {
        state.update { EncryptionManagerState.Initial }
    }

    override suspend fun createCipherInputStream(
        password: String?,
        input: InputStream
    ): CipherInputStream = withContext(IO) {
        val passwordToUse = password ?: (state.value as? EncryptionManagerState.Ready)?.password ?: error("EncryptionManager not initialized")

        suspendCoroutine { continuation ->
            val version = input.read().toByte()
            if (version != ENC_VERSION_BYTE) throw IllegalArgumentException("Unsupported version")

            val salt = ByteArray(SALT_SIZE)
            val iv = ByteArray(IV_SIZE)

            input.read(salt, 0, salt.size)
            input.read(iv, salt.size, iv.size)

            val key = deriveAesKey(passwordToUse, salt)
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))

            CipherInputStream(input, cipher)
        }
    }

    override suspend fun createCipherOutputStream(
        password: String?,
        output: OutputStream
    ): CipherOutputStream = withContext(IO) {
        val passwordToUse = password ?: (state.value as? EncryptionManagerState.Ready)?.password ?: error("EncryptionManager not initialized")

        suspendCoroutine { continuation ->
            val salt = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }
            val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }

            val key = deriveAesKey(passwordToUse, salt)
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))

            output.write(byteArrayOf(ENC_VERSION_BYTE))
            output.write(salt)
            output.write(iv)

            CipherOutputStream(output, cipher)
        }
    }

    private fun deriveAesKey(password: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance(FULL_ALGORITHM)
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_SIZE)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, ALGORITHM)
    }
}