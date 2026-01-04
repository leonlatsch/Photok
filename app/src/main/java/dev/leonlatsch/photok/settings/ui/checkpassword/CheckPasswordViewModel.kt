package dev.leonlatsch.photok.settings.ui.checkpassword

import android.app.Application
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.other.extensions.empty
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.uicomponnets.bindings.ObservableViewModel
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt
import javax.inject.Inject

@HiltViewModel
class CheckPasswordViewModel @Inject constructor(
    app: Application,
    private val config: Config,
) : ObservableViewModel(app) {

    @get:Bindable
    var checkPasswordState: CheckPasswordState = CheckPasswordState.START
        set(value) {
            field = value
            notifyChange(BR.checkPasswordState, value)
        }

    @Bindable
    var oldPassword: String = String.empty
        set(value) {
            field = value
            notifyChange(BR.oldPassword, value)
        }

    /**
     * Checks if the password is valid and updates state. For security concerns.
     * Called by ui.
     */
    fun checkOld() = viewModelScope.launch {
        checkPasswordState = CheckPasswordState.CHECKING_OLD

        val storedPassword = config.securityPassword
        storedPassword ?: return@launch

        checkPasswordState = if (BCrypt.checkpw(oldPassword, storedPassword)) {
            CheckPasswordState.OLD_VALID
        } else {
            CheckPasswordState.OLD_INVALID
        }
    }
}