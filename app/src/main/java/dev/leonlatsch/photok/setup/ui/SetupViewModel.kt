package dev.leonlatsch.photok.setup.ui

import android.app.Application
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.other.extensions.empty
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.security.PasswordManager
import dev.leonlatsch.photok.security.PasswordUtils
import dev.leonlatsch.photok.uicomponnets.bindings.ObservableViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the setup.
 * Handles password validation, saving password, initializing the [EncryptionManager], etc.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class SetupViewModel @Inject constructor(
    app: Application,
    val encryptionManager: EncryptionManager,
    private val passwordManager: PasswordManager
) : ObservableViewModel(app) {

    //region binding properties

    @Bindable
    var password: String = String.empty
        set(value) {
            field = value
            notifyChange(BR.password, value)
        }

    @Bindable
    var confirmPassword: String = String.empty
        set(value) {
            field = value
            notifyChange(BR.confirmPassword, value)
        }

    @get:Bindable
    var setupState: SetupState = SetupState.SETUP
        set(value) {
            field = value
            notifyChange(BR.setupState, value)
        }

    // endregion

    /**
     * Save the password to database.
     * Validates both passwords.
     * Hashes and saves the password.
     * Initializes [EncryptionManager].
     * Called by ui.
     */
    fun savePassword() = viewModelScope.launch {
        setupState = SetupState.LOADING

        setupState = if (validateBothPasswords()) {
            passwordManager.storePassword(password)
            encryptionManager.initialize(this@SetupViewModel.password)
            SetupState.FINISHED
        } else {
            SetupState.SETUP
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