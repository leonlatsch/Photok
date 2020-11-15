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

import android.app.Application
import androidx.databinding.Bindable
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.other.emptyString
import dev.leonlatsch.photok.security.PasswordUtils
import dev.leonlatsch.photok.settings.Config
import dev.leonlatsch.photok.ui.components.bindings.ObservableViewModel
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
    app: Application,
    private val config: Config
) : ObservableViewModel(app) {

    @get:Bindable
    var changePasswordState: ChangePasswordState = ChangePasswordState.START
        set(value) {
            field = value
            notifyChange(BR.changePasswordState, value)
        }

    @Bindable
    var oldPassword: String = emptyString()
        set(value) {
            field = value
            notifyChange(BR.oldPassword, value)
        }

    @Bindable
    var newPassword: String = emptyString()
        set(value) {
            field = value
            notifyChange(BR.newPassword, value)
        }

    @Bindable
    var newPasswordConfirm: String = emptyString()
        set(value) {
            field = value
            notifyChange(BR.newPasswordConfirm, value)
        }

    /**
     * Checks if the old password is valid and updates state. For security concerns.
     * Called by ui.
     */
    fun checkOld() = viewModelScope.launch {
        changePasswordState = ChangePasswordState.CHECKING_OLD

        val storedPassword = config.securityPassword
        storedPassword ?: return@launch

        changePasswordState = if (BCrypt.checkpw(oldPassword, storedPassword)) {
            ChangePasswordState.OLD_VALID
        } else {
            ChangePasswordState.OLD_INVALID
        }
    }

    /**
     * Checks if the entered password is valid and updates state.
     * Called by ui.
     */
    fun checkNew() = viewModelScope.launch {
        changePasswordState = if (PasswordUtils.validatePasswords(
                newPassword,
                newPasswordConfirm
            )
        ) {
            ChangePasswordState.NEW_VALID
        } else {
            ChangePasswordState.NEW_INVALID
        }
    }
}