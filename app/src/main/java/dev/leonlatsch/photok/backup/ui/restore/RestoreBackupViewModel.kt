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
import dev.leonlatsch.photok.io.IO
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val RESTORE_BACKUP_URI = "restore_backup_uri"

sealed interface RestoreBackupUiState {
    data class Validating(
        val fileName: String,
    ) : RestoreBackupUiState

    data class Overview(
        val validation: BackupValidation,
        val emptyVault: Boolean,
    ) : RestoreBackupUiState

    data class Unlock(
        val fileName: String,
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
    private val photoRepository: PhotoRepository,
    private val io: IO,
): ViewModel() {

    private val validation = MutableStateFlow<BackupValidation?>(null)
    private val password = MutableStateFlow("")
    private val unlocking = MutableStateFlow(false)

    private val validatingState = RestoreBackupUiState.Validating(
        fileName = io.getFileName(restoreBackupUri).orEmpty()
    )

    val uiState = combine(
        password,
        validation,
        unlocking,
    ) { password, validation, unlocking ->
        when {
            validation == null -> validatingState
            unlocking -> RestoreBackupUiState.Unlock(
                fileName = validation.fileName,
                password = password,
            )
            else -> RestoreBackupUiState.Overview(
                validation = validation,
                emptyVault = photoRepository.countAll() == 0,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), validatingState)

    fun handleUiEvent(event: RestoreBackupUiEvent) {
        when (event) {
            is RestoreBackupUiEvent.UnlockBackupClicked -> unlocking.update { true }

            is RestoreBackupUiEvent.BackToOverviewClicked -> {
                password.update { "" }
                unlocking.update { false }
            }

            is RestoreBackupUiEvent.PasswordChanged -> password.update { event.password }

            is RestoreBackupUiEvent.ConfirmPasswordClicked -> {
                // TODO: unlock backup and move to Restoring
            }
        }
    }

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