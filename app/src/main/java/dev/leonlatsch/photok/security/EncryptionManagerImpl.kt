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

import dev.leonlatsch.photok.other.AES
import dev.leonlatsch.photok.other.AES_ALGORITHM
import dev.leonlatsch.photok.other.IV_SIZE
import dev.leonlatsch.photok.other.SHA_256
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

sealed interface EncryptionManagerState {
    data object Initial : EncryptionManagerState
    data object Error : EncryptionManagerState

    data class Ready(val key: SecretKeySpec) : EncryptionManagerState
}

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
                EncryptionManagerState.Ready(key = genSecKey(password))
            }
        } catch (e: GeneralSecurityException) {
            Timber.d("Error initializing EncryptionManager: $e")
            state.update { EncryptionManagerState.Error }
        }
    }

    override fun reset() {
        state.update { EncryptionManagerState.Initial }
    }

    override fun createEncryptionCipher(password: String?): Cipher? {
        val key = password?.let { genSecKey(it) } ?: (state.value as? EncryptionManagerState.Ready)?.key ?: return null

        return createEncryptionCipher(key)
    }

    override fun createDecryptionCipher(ivBytes: ByteArray, password: String?): Cipher? {
        val key = password?.let { genSecKey(it) } ?: (state.value as? EncryptionManagerState.Ready)?.key ?: return null

        return createDecryptionCipher(key, ivBytes)
    }

    private fun createDecryptionCipher(secretKeySpec: SecretKeySpec?, ivBytes: ByteArray): Cipher? {
        return if (isReady) try {
            Cipher.getInstance(AES_ALGORITHM).apply {
                init(Cipher.DECRYPT_MODE, secretKeySpec, IvParameterSpec(ivBytes))
            }
        } catch (e: GeneralSecurityException) {
            Timber.d("Error initializing cipher: $e")
            null
        } else {
            Timber.d("EncryptionManager has to be ready to create a cipher")
            null
        }
    }

    private fun createEncryptionCipher(secretKeySpec: SecretKeySpec?): Cipher? {
        return if (isReady) try {
            Cipher.getInstance(AES_ALGORITHM).apply {
                init(Cipher.ENCRYPT_MODE, secretKeySpec, IvParameterSpec(genIv()))
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

    private fun genIv(): ByteArray {
        val iv = ByteArray(IV_SIZE)
        SecureRandom().nextBytes(iv)
        return iv
    }
}