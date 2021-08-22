/*
 *   Copyright 2020-2021 Leon Latsch
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

package dev.leonlatsch.photok.unlock.ui

import android.app.Application
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.other.DEBUG_PASSWORD
import dev.leonlatsch.photok.other.extensions.empty
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.security.PasswordManager
import dev.leonlatsch.photok.uicomponnets.bindings.ObservableViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for unlocking the safe.
 * Handles state, password validation and initializing the [EncryptionManager].
 * Just like the setup.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class UnlockViewModel @Inject constructor(
    app: Application,
    val encryptionManager: EncryptionManager,
    private val passwordManager: PasswordManager
) : ObservableViewModel(app) {

    @Bindable
    var password: String = String.empty
        set(value) {
            field = value
            notifyChange(BR.password, value)
        }

    @get:Bindable
    var unlockState: UnlockState = UnlockState.UNDEFINED
        set(value) {
            field = value
            notifyChange(BR.unlockState, value)
        }

    /**
     * Tries to unlock the save.
     * Compares [password] to saved hash.
     * Updates UnlockState.
     * Called by ui.
     */
    fun unlock() = viewModelScope.launch {
        unlockState = UnlockState.CHECKING

        unlockState = if (passwordManager.checkPassword(password)) {
            encryptionManager.initialize(password)
            UnlockState.UNLOCKED
        } else {
            UnlockState.LOCKED
        }
    }

    fun debugUnlock(): Boolean {
        viewModelScope.launch {
            if (BuildConfig.DEBUG) {
                encryptionManager.initialize(DEBUG_PASSWORD)
                unlockState = UnlockState.UNLOCKED
            }
        }
        return true
    }
}