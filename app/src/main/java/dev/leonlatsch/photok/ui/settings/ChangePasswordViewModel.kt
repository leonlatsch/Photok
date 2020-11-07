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

package dev.leonlatsch.photok.ui.settings

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.leonlatsch.photok.other.emptyString
import dev.leonlatsch.photok.security.PasswordUtils
import dev.leonlatsch.photok.settings.Config
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt

/**
 * ViewModel for changing the password.
 * Validates old and new passwords and starts ReEncryptBottomSheetDialogFragment.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class ChangePasswordViewModel @ViewModelInject constructor(
    private val config: Config
) : ViewModel() {

    val changePasswordState: MutableLiveData<ChangePasswordState> =
        MutableLiveData(ChangePasswordState.START)

    val oldPasswordTextValue: MutableLiveData<String> = MutableLiveData(emptyString())
    val newPasswordTextValue: MutableLiveData<String> = MutableLiveData(emptyString())
    val newPasswordConfirmTextValue: MutableLiveData<String> = MutableLiveData(emptyString())

    /**
     * Checks if the old password is valid and updates state. For security concerns.
     * Called by ui.
     */
    fun checkOld() = viewModelScope.launch {
        changePasswordState.postValue(ChangePasswordState.CHECKING_OLD)

        val storedPassword = config.securityPassword
        storedPassword ?: return@launch

        if (BCrypt.checkpw(oldPasswordTextValue.value!!, storedPassword)) {
            changePasswordState.postValue(ChangePasswordState.OLD_VALID)
        } else {
            changePasswordState.postValue(ChangePasswordState.OLD_INVALID)
        }
    }

    /**
     * Checks if the entered password is valid and updates state.
     * Called by ui.
     */
    fun checkNew() = viewModelScope.launch {
        if (PasswordUtils.validatePasswords(
                newPasswordTextValue,
                newPasswordConfirmTextValue
            )
        ) {
            changePasswordState.postValue(ChangePasswordState.NEW_VALID)
        } else {
            changePasswordState.postValue(ChangePasswordState.NEW_INVALID)
        }
    }
}