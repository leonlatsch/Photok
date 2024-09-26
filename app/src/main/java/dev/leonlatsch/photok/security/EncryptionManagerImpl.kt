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
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.inject.Inject

sealed interface EncryptionManagerState {
    data object Initial : EncryptionManagerState
    data class Ready(val encryptionKey: SecretKey) : EncryptionManagerState
}

class EncryptionManagerImpl @Inject constructor(
    private val keyStorage: KeyStorage,
) : EncryptionManager {

    private val state = MutableStateFlow<EncryptionManagerState>(EncryptionManagerState.Initial)

    // Needed for legacy usage
    override var isReady: Boolean = state.value is EncryptionManagerState.Ready

    override fun initialize(password: String) {
        if (password.length < 6) {
            isReady = false
            return
        }

        keyStorage.getOrCreateKey()
            .onSuccess { secretKey ->
                state.update { EncryptionManagerState.Ready(secretKey) }
            }
            .onFailure {
                Timber.d("Error initializing EncryptionManager: $it")
            }
    }

    override fun reset() {
        state.update { EncryptionManagerState.Initial }
    }

    override fun createCipherInputStream(
        inputStream: InputStream,
        password: String?
    ): CipherInputStream? {
        TODO("Not yet implemented")
    }

    override fun createCipherOutputStream(
        outputStream: OutputStream,
        password: String?
    ): CipherOutputStream? {
        TODO("Not yet implemented")
    }

}