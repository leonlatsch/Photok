package dev.leonlatsch.photok.backup.ui

import android.app.Application
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.other.extensions.empty
import dev.leonlatsch.photok.security.PasswordManager
import dev.leonlatsch.photok.uicomponnets.bindings.ObservableViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for unlocking a Backup.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class UnlockBackupViewModel @Inject constructor(
    app: Application,
    private val passwordManager: PasswordManager
) : ObservableViewModel(app) {

    @Bindable
    var password: String = String.empty
        set(value) {
            field = value
            notifyChange(BR.password, value)
        }

    /**
     * Verifies the password and calls [result] with true/false.
     */
    fun verifyPassword(backupPassword: String, result: (valid: Boolean) -> Unit) =
        viewModelScope.launch {
            val valid = passwordManager.checkPassword(password, backupPassword)
            result(valid)
        }
}