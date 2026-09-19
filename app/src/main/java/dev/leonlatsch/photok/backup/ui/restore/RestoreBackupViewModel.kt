package dev.leonlatsch.photok.backup.ui.restore

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.backup.data.BackupMetaData
import dev.leonlatsch.photok.backup.domain.BackupValidation
import dev.leonlatsch.photok.backup.domain.RestoreBackupV1
import dev.leonlatsch.photok.backup.domain.RestoreBackupV2
import dev.leonlatsch.photok.backup.domain.RestoreBackupV3
import dev.leonlatsch.photok.backup.domain.RestoreBackupV4
import dev.leonlatsch.photok.backup.domain.RestoreBackupV5
import dev.leonlatsch.photok.backup.domain.UnlockBackupUseCase
import dev.leonlatsch.photok.backup.domain.ValidateBackupUseCase
import dev.leonlatsch.photok.encryption.domain.models.Session
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
    private var unlockBackupUseCase: UnlockBackupUseCase,
    private val photoRepository: PhotoRepository,
    private val io: IO,
    private val v1Strategy: RestoreBackupV1,
    private val v2Strategy: RestoreBackupV2,
    private val v3Strategy: RestoreBackupV3,
    private val v4Strategy: RestoreBackupV4,
    private val v5Strategy: RestoreBackupV5,
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
                .onFailure {
                    // TODO
                }
        }
    }

    private fun unlockBackup() = viewModelScope.launch {
        val metaData = validation.value?.metaData ?: return@launch

        unlockBackupUseCase(restoreBackupUri, metaData, password.value)
            .onSuccess { session ->
                restoreBackup(metaData, session)
            }
            .onFailure {
                // TODO
            }
    }

    private fun restoreBackup(metaData: BackupMetaData, session: Session) = viewModelScope.launch {
        val zipInputStream = io.zip.openZipInput(restoreBackupUri)

        val result = when (metaData) {
            is BackupMetaData.V1 -> v1Strategy.restore(metaData, zipInputStream, session)
            is BackupMetaData.V2 -> v2Strategy.restore(metaData, zipInputStream, session)
            is BackupMetaData.V3 -> v3Strategy.restore(metaData, zipInputStream, session)
            is BackupMetaData.V4 -> v4Strategy.restore(metaData, zipInputStream, session)
            is BackupMetaData.V5 -> v5Strategy.restore(metaData, zipInputStream, session)
        }

        zipInputStream.close()
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted(RESTORE_BACKUP_URI) restoreBackupUri: Uri): RestoreBackupViewModel
    }
}