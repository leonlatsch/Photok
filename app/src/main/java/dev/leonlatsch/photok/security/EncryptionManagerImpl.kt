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
import javax.inject.Singleton
import kotlin.io.encoding.Base64


private const val ENC_VERSION_BYTE: Byte = 0x01
private const val IV_SIZE = 16
const val SALT_SIZE = 16
private const val KEY_SIZE = 256
private const val ITERATION_COUNT = 100_000
private const val FULL_ALGORITHM = "PBKDF2WithHmacSHA256"
private const val ALGORITHM = "AES"


/**
 * Encryption Format V1
 *
 *  Each encrypted file/stream follows the structure:
 *
 *  ┌───────────────────────────────┐
 *  │         Encrypted Data        │
 *  ├───────────────────────────────┤
 *  │ ENC_VERSION_BYTE (1 byte)     │  ← Format version, must equal 0x01
 *  │ SALT (16 bytes)               │  ← Random salt for PBKDF2
 *  │ IV (16 bytes)                 │  ← Initialization vector for AES
 *  │ ENCRYPTED_DATA (N bytes)      │  ← AES-256-CBC/PKCS7 encrypted payload
 *  └───────────────────────────────┘
 *
 * Short: [ENC_VERSION_BYTE][SALT][IV][ENCRYPTED_DATA]
 *
 * Notes:
 *  - Key Derivation:
 *      • Algorithm: PBKDF2WithHmacSHA256
 *      • Iterations: 100,000
 *      • Key Size: 256 bits
 *  - Encryption:
 *      • Algorithm: AES
 *      • Mode: CBC
 *      • Padding: PKCS7
 *  - Password Handling:
 *      • A user-supplied password (≥ 6 characters) is combined with a
 *        per-user salt to derive the AES key.
 *      • If no password is provided, the cached/initialized key is required.
 *  - Caching:
 *      • Derived keys can be cached in-memory (configurable).
 *      • Cache is cleared when disabled or when the manager is reset.
 *  - Error Handling:
 *      • Initialization fails if password is too short (<6) or if key derivation fails.
 *      • Unsupported version bytes cause decryption errors.
 *
 * Versioning:
 *  - This format is defined as version 1 (ENC_VERSION_BYTE = 0x01).
 *  - Future formats must increment the version byte and adjust parsing accordingly.
 */
@Singleton
class EncryptionManagerImpl @Inject constructor(
    private val getOrCreateUserSalt: GetOrCreateUserSaltUseCase
) : EncryptionManager {

    private val keyCache = mutableMapOf<String, SecretKey>()

    private val state: MutableStateFlow<State> =
        MutableStateFlow(State.Initial)

    override val isReady: Boolean
        get() = state.value is State.Ready

    override var keyCacheEnabled: Boolean = false
        set(value) {
            keyCache.clear()
            field = value
        }

    override fun initialize(password: String): Result<Unit> {
        if (password.length < 6) {
            state.update { State.Error }
            return Result.failure(IllegalArgumentException("Password too short"))
        }
        try {
            state.update {
                State.Ready(
                    key = deriveAesKey(password, getOrCreateUserSalt())
                )
            }
            return Result.success(Unit)
        } catch (e: GeneralSecurityException) {
            Timber.d("Error initializing EncryptionManager: $e")
            state.update { State.Error }
            return Result.failure(e)
        }
    }

    override fun initializeWithBiometrics(): Result<Unit> {
        // TODO
        // get key from prefs
        // decrypt wrapped key with key from keystore
        // set state to ready with decrypted key
        return initialize("abc123")
    }

    override fun reset() {
        state.update { State.Initial }
        keyCache.clear()
    }

    override fun createCipherInputStream(
        input: InputStream,
        password: String?,
    ): CipherInputStream? {
        try {
            val version = input.read().toByte()
            if (version != ENC_VERSION_BYTE) throw IllegalArgumentException("Unsupported version")

            val iv = ByteArray(IV_SIZE)
            val salt = ByteArray(SALT_SIZE)

            input.read(salt, 0, salt.size)
            input.read(iv, 0, iv.size)

            val key = if (password != null) {
                deriveAesKey(password, salt)
            } else {
                requireKey()
            }

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))

            return CipherInputStream(input, cipher)
        } catch (e: Exception) {
            Timber.d("Error creating CipherInputStream: $e")
            return null
        }
    }

    override fun createCipherOutputStream(
        output: OutputStream,
        password: String?,
    ): CipherOutputStream? {

        try {
            val salt = getOrCreateUserSalt()
            val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }

            val key = if (password != null) {
                deriveAesKey(password, salt)
            } else {
                requireKey()
            }

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

    private fun requireKey(): SecretKey {
        return (state.value as? State.Ready)?.key ?: error("EncryptionManager not initialized")
    }

    private fun deriveAesKey(password: String, salt: ByteArray): SecretKey {
        if (keyCacheEnabled) {
            val hash = "${password}_${Base64.encode(salt)}"
            keyCache[hash]?.let { return it }
        }

        val factory = SecretKeyFactory.getInstance(FULL_ALGORITHM)
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_SIZE)
        val keyBytes = factory.generateSecret(spec).encoded

        return SecretKeySpec(keyBytes, ALGORITHM).also {
            if (keyCacheEnabled) {
                val hash = "${password}_${Base64.encode(salt)}"
                keyCache[hash] = it
            }
        }
    }

    private sealed interface State {
        data object Initial : State
        data object Error : State

        data class Ready(
            val key: SecretKey,
        ) : State
    }
}
