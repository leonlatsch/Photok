package dev.leonlatsch.photok.backup.ui.restore

import android.app.Activity

sealed interface RestoreBackupUiEvent {

    /** Overview -> Unlock */
    data object UnlockBackupClicked : RestoreBackupUiEvent

    /** Unlock -> Overview */
    data object BackToOverviewClicked : RestoreBackupUiEvent

    data class PasswordChanged(val password: String) : RestoreBackupUiEvent

    data object ConfirmPasswordClicked : RestoreBackupUiEvent

    data class DoneClicked(val activity: Activity?) : RestoreBackupUiEvent
}
