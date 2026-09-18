package dev.leonlatsch.photok.backup.ui.restore

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.backup.domain.BackupValidation
import dev.leonlatsch.photok.backup.domain.ValidateBackupUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val RESTORE_BACKUP_URI = "restore_backup_uri"

sealed interface RestoreBackupUiState {
    data object Validating : RestoreBackupUiState

    data class Overview(
        val validation: BackupValidation
    ) : RestoreBackupUiState

    data class Unlock(
        val password: String,
    ) : RestoreBackupUiState

    data class Restoring(
        val progress: Float,
        // TODO: speed
        // TODO: bytes written
        // TODO: time remaining / elapsed
        // TODO: success/error list
    ) : RestoreBackupUiState

    // TODO: Finished state
}

@HiltViewModel(assistedFactory = RestoreBackupViewModel.Factory::class)
class RestoreBackupViewModel @AssistedInject constructor(
    @Assisted(RESTORE_BACKUP_URI) private val restoreBackupUri: Uri,
    private val validateBackupUseCase: ValidateBackupUseCase,
): ViewModel() {

    private val validation = MutableStateFlow<BackupValidation?>(null)
    private val password = MutableStateFlow("")

    val uiState = combine(
        password,
        validation
    ) { password, validation ->
        when {
            validation == null -> RestoreBackupUiState.Validating
            else -> RestoreBackupUiState.Overview(validation)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), RestoreBackupUiState.Validating)

    init {
        viewModelScope.launch {
            validateBackupUseCase(restoreBackupUri)
                .onSuccess { validation ->
                    this@RestoreBackupViewModel.validation.update { validation }
                }
                .onFailure { }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted(RESTORE_BACKUP_URI) restoreBackupUri: Uri): RestoreBackupViewModel
    }
}