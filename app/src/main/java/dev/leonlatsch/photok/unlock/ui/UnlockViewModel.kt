package dev.leonlatsch.photok.unlock.ui

import android.app.Application
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.other.extensions.empty
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.security.PasswordManager
import dev.leonlatsch.photok.security.migration.LegacyEncryptionManager
import dev.leonlatsch.photok.uicomponnets.bindings.ObservableViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
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
    @LegacyEncryptionManager private val legacyEncryptionManager: EncryptionManager,
    private val passwordManager: PasswordManager,
) : ObservableViewModel(app) {

    @Bindable
    var password: String = String.empty
        set(value) {
            field = value
            notifyChange(BR.password, value)
        }

    val unlockState: MutableStateFlow<UnlockState> = MutableStateFlow(UnlockState.UNDEFINED)

    /**
     * Tries to unlock the save.
     * Compares [password] to saved hash.
     * Updates UnlockState.
     * Called by ui.
     */
    fun unlock() = viewModelScope.launch {
        unlockState.update { UnlockState.CHECKING }

        unlockState.update {
            if (passwordManager.checkPassword(password)) {
                encryptionManager.initialize(password)
                legacyEncryptionManager.initialize(password)

                UnlockState.UNLOCKED
            } else {
                UnlockState.LOCKED
            }
        }
    }

}