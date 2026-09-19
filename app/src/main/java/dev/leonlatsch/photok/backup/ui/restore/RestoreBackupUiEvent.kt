package dev.leonlatsch.photok.backup.ui.restore

sealed interface RestoreBackupUiEvent {

    /** Overview -> Unlock */
    data object UnlockBackupClicked : RestoreBackupUiEvent

    /** Unlock -> Overview */
    data object BackToOverviewClicked : RestoreBackupUiEvent

    data class PasswordChanged(val password: String) : RestoreBackupUiEvent

    data object ConfirmPasswordClicked : RestoreBackupUiEvent
}
