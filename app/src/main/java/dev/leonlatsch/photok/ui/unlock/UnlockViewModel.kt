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

package dev.leonlatsch.photok.ui.unlock

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.leonlatsch.photok.model.repositories.PasswordRepository
import dev.leonlatsch.photok.other.emptyString
import dev.leonlatsch.photok.security.EncryptionManager
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt

/**
 * ViewModel for unlocking the safe.
 * Handles state, password validation and initializing the [EncryptionManager].
 * Just like the setup.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class UnlockViewModel @ViewModelInject constructor(
    private val passwordRepository: PasswordRepository,
    val encryptionManager: EncryptionManager
) : ViewModel() {

    var passwordText: MutableLiveData<String> = MutableLiveData(emptyString())
    var unlockState: MutableLiveData<UnlockState> = MutableLiveData(UnlockState.UNDEFINED)

    fun unlock() = viewModelScope.launch {
        unlockState.postValue(UnlockState.CHECKING)

        val savedPassword = passwordRepository.getPassword()
        if (BCrypt.checkpw(passwordText.value, savedPassword?.password)) {
            encryptionManager.initialize(passwordText.value!!)
            unlockState.postValue(UnlockState.UNLOCKED)
        } else {
            unlockState.postValue(UnlockState.LOCKED)
        }
    }

}