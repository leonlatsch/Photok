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
import dev.leonlatsch.photok.backup.domain.RestoreResult
import dev.leonlatsch.photok.backup.domain.UnlockBackupUseCase
import dev.leonlatsch.photok.backup.domain.ValidateBackupUseCase
import dev.leonlatsch.photok.encryption.domain.models.Session
import dev.leonlatsch.photok.io.IO
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
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
        val fileName: String,
        // TODO: progress
        // TODO: speed
        // TODO: bytes written
        // TODO: time remaining / elapsed
        // TODO: success/error list
    ) : RestoreBackupUiState

    data class Finished(
        val fileName: String,
        val errors: Int,
    ) : RestoreBackupUiState

    /** The step the user is on. [Validating] is shown until the backup was validated. */
    enum class Step {
        Overview,
        Unlock,
        Restoring,
        Finished,
    }

    data class Inputs(
        val step: Step = Step.Overview,
        val password: String = "",
        val restoreResult: RestoreResult? = null,
    )
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
) : ViewModel() {

    private val fileName = io.getFileName(restoreBackupUri).orEmpty()

    private val inputs = MutableStateFlow(RestoreBackupUiState.Inputs())

    /** `null` until the backup was validated. */
    private val validation = flow {
        emit(validateBackupUseCase(restoreBackupUri).getOrNull()) // TODO: error state
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val emptyVault = flow {
        emit(photoRepository.countAll() == 0)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val validatingState = RestoreBackupUiState.Validating(fileName = fileName)

    val uiState = combine(
        inputs,
        validation,
        emptyVault,
    ) { inputs, validation, emptyVault ->
        when {
            validation == null -> validatingState

            inputs.step == RestoreBackupUiState.Step.Unlock -> RestoreBackupUiState.Unlock(
                fileName = fileName,
                password = inputs.password,
            )

            inputs.step == RestoreBackupUiState.Step.Restoring -> RestoreBackupUiState.Restoring(
                fileName = fileName,
            )

            inputs.step == RestoreBackupUiState.Step.Finished -> RestoreBackupUiState.Finished(
                fileName = fileName,
                errors = inputs.restoreResult?.errors ?: 0,
            )

            else -> RestoreBackupUiState.Overview(
                validation = validation,
                emptyVault = emptyVault,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), validatingState)

    fun handleUiEvent(event: RestoreBackupUiEvent) {
        when (event) {
            is RestoreBackupUiEvent.UnlockBackupClicked -> inputs.update {
                it.copy(step = RestoreBackupUiState.Step.Unlock)
            }

            is RestoreBackupUiEvent.BackToOverviewClicked -> inputs.update {
                it.copy(step = RestoreBackupUiState.Step.Overview, password = "")
            }

            is RestoreBackupUiEvent.PasswordChanged -> inputs.update {
                it.copy(password = event.password)
            }

            is RestoreBackupUiEvent.ConfirmPasswordClicked -> unlockAndRestore()
        }
    }

    private fun unlockAndRestore() = viewModelScope.launch {
        val metaData = validation.value?.metaData ?: return@launch
        val password = inputs.value.password

        inputs.update { it.copy(step = RestoreBackupUiState.Step.Restoring, password = "") }

        unlockBackupUseCase(restoreBackupUri, metaData, password)
            .onSuccess { session ->
                restoreBackup(metaData, session)
            }
            .onFailure {
                // TODO: error state
            }
    }

    private suspend fun restoreBackup(metaData: BackupMetaData, session: Session) {
        val zipInputStream = io.zip.openZipInput(restoreBackupUri)

        val result = when (metaData) {
            is BackupMetaData.V1 -> v1Strategy.restore(metaData, zipInputStream, session)
            is BackupMetaData.V2 -> v2Strategy.restore(metaData, zipInputStream, session)
            is BackupMetaData.V3 -> v3Strategy.restore(metaData, zipInputStream, session)
            is BackupMetaData.V4 -> v4Strategy.restore(metaData, zipInputStream, session)
            is BackupMetaData.V5 -> v5Strategy.restore(metaData, zipInputStream, session)
        }

        zipInputStream.close()

        inputs.update {
            it.copy(step = RestoreBackupUiState.Step.Finished, restoreResult = result)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted(RESTORE_BACKUP_URI) restoreBackupUri: Uri): RestoreBackupViewModel
    }
}
