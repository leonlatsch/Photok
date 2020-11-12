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

package dev.leonlatsch.photok.ui.setup

import android.app.Application
import androidx.databinding.Bindable
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.other.emptyString
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.security.PasswordUtils
import dev.leonlatsch.photok.settings.Config
import dev.leonlatsch.photok.ui.components.bindings.ObservableViewModel
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt

/**
 * ViewModel for the setup.
 * Handles password validation, saving password, initializing the [EncryptionManager], etc.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class SetupViewModel @ViewModelInject constructor(
    app: Application,
    private val config: Config,
    private val encryptionManager: EncryptionManager
) : ObservableViewModel(app) {

    //region binding properties

    @Bindable
    var password: String = emptyString()
        set(value) {
            field = value
            notifyChange(BR.password, value)
        }

    @Bindable
    var confirmPassword: String = emptyString()
        set(value) {
            field = value
            notifyChange(BR.confirmPassword, value)
        }

    // endregion

    val setupState: MutableLiveData<SetupState> = MutableLiveData(SetupState.SETUP)

    /**
     * Save the password to database.
     * Validates both passwords.
     * Hashes and saves the password.
     * Initializes [EncryptionManager].
     * Called by ui.
     */
    fun savePassword() = viewModelScope.launch {
        setupState.postValue(SetupState.LOADING)

        if (validateBothPasswords()) {
            val password = BCrypt.hashpw(password, BCrypt.gensalt())
            config.securityPassword = password
            encryptionManager.initialize(this@SetupViewModel.password)
            setupState.postValue(SetupState.FINISHED)
        } else {
            setupState.postValue(SetupState.SETUP)
        }
    }

    /**
     * Validate hte [password] property.
     */
    fun validatePassword() = PasswordUtils.validatePassword(password)

    /**
     * @see PasswordUtils.passwordsNotEmptyAndEqual
     */
    fun passwordsEqual() =
        PasswordUtils.passwordsNotEmptyAndEqual(password, confirmPassword)

    /**
     * @see PasswordUtils.validatePasswords
     */
    fun validateBothPasswords() = PasswordUtils.validatePasswords(password, confirmPassword)
}