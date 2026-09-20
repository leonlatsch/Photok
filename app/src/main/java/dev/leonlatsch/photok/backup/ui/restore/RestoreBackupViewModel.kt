package dev.leonlatsch.photok.backup.ui.restore

import android.app.Activity
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
import dev.leonlatsch.photok.backup.domain.RestoreProgress
import dev.leonlatsch.photok.backup.domain.RestoreResult
import dev.leonlatsch.photok.backup.domain.UnlockBackupUseCase
import dev.leonlatsch.photok.backup.domain.ValidateBackupUseCase
import dev.leonlatsch.photok.encryption.domain.models.Session
import dev.leonlatsch.photok.io.IO
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.review.InAppReview
import dev.leonlatsch.photok.review.ReviewTrigger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val RESTORE_BACKUP_URI = "restore_backup_uri"

private const val LOG_ENTRIES = RESTORE_LOG_ROWS

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
        val unlocking: Boolean,
    ) : RestoreBackupUiState

    data class Restoring(
        val fileName: String,
        val filesDone: Int,
        val filesTotal: Int,
        val bytesDone: Long,
        val bytesTotal: Long,
        val bytesPerSecond: Long,
        val millisRemaining: Long?,
        val log: List<RestoreLogEntry>,
    ) : RestoreBackupUiState {
        val progress: Float =
            if (bytesTotal == 0L) 0f else bytesDone.toFloat() / bytesTotal.toFloat()
    }

    data class Finalizing(
        val fileName: String,
    ) : RestoreBackupUiState

    data class Finished(
        val fileName: String,
        val failedFiles: List<String>,
    ) : RestoreBackupUiState

    enum class Step {
        Overview,
        Unlock,
        Restoring,
        Finished,
    }

    data class Inputs(
        val step: Step = Step.Overview,
        val password: String = "",
        val unlocking: Boolean = false,
        val progress: RestoreProgress? = null,
        val log: List<RestoreLogEntry> = emptyList(),
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
    private val inAppReview: InAppReview,
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
                unlocking = inputs.unlocking,
            )

            inputs.step == RestoreBackupUiState.Step.Restoring ->
                restoringState(inputs.progress, inputs.log)

            inputs.step == RestoreBackupUiState.Step.Finished -> RestoreBackupUiState.Finished(
                fileName = fileName,
                failedFiles = inputs.restoreResult?.failedFiles.orEmpty(),
            )

            else -> RestoreBackupUiState.Overview(
                validation = validation,
                emptyVault = emptyVault,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), validatingState)

    private fun restoringState(
        progress: RestoreProgress?,
        log: List<RestoreLogEntry>,
    ): RestoreBackupUiState =
        when (progress) {
            is RestoreProgress.Finalizing -> RestoreBackupUiState.Finalizing(fileName = fileName)

            is RestoreProgress.Restoring -> RestoreBackupUiState.Restoring(
                fileName = fileName,
                filesDone = progress.filesDone,
                filesTotal = progress.filesTotal,
                bytesDone = progress.bytesDone,
                bytesTotal = progress.bytesTotal,
                bytesPerSecond = progress.bytesPerSecond,
                millisRemaining = progress.millisRemaining,
                log = log,
            )

            // Restore was started, the first progress has not arrived yet
            else -> RestoreBackupUiState.Restoring(
                fileName = fileName,
                filesDone = 0,
                filesTotal = 0,
                bytesDone = 0,
                bytesTotal = 0,
                bytesPerSecond = 0,
                millisRemaining = null,
                log = emptyList(),
            )
        }

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

            is RestoreBackupUiEvent.DoneClicked -> requestInAppReview(event.activity)
        }
    }

    private fun requestInAppReview(activity: Activity?) {
        activity ?: return

        val failedFiles = inputs.value.restoreResult?.failedFiles ?: return
        if (failedFiles.isNotEmpty()) return

        inAppReview.requestInAppReview(activity, ReviewTrigger.BackupRestored)
    }

    private fun unlockAndRestore() = viewModelScope.launch {
        val metaData = validation.value?.metaData ?: return@launch
        val password = inputs.value.password

        inputs.update { it.copy(unlocking = true) }

        unlockBackupUseCase(restoreBackupUri, metaData, password)
            .onSuccess { session ->
                inputs.update {
                    it.copy(
                        step = RestoreBackupUiState.Step.Restoring,
                        password = "",
                        unlocking = false,
                    )
                }

                restoreBackup(metaData, session)
            }
            .onFailure {
                // TODO: error state
                inputs.update { it.copy(unlocking = false) }
            }
    }

    private suspend fun restoreBackup(metaData: BackupMetaData, session: Session) {
        val zipInputStream = io.zip.openZipInput(restoreBackupUri)

        val progressFlow = when (metaData) {
            is BackupMetaData.V1 -> v1Strategy.restore(metaData, zipInputStream, session)
            is BackupMetaData.V2 -> v2Strategy.restore(metaData, zipInputStream, session)
            is BackupMetaData.V3 -> v3Strategy.restore(metaData, zipInputStream, session)
            is BackupMetaData.V4 -> v4Strategy.restore(metaData, zipInputStream, session)
            is BackupMetaData.V5 -> v5Strategy.restore(metaData, zipInputStream, session)
        }

        progressFlow.collect { progress ->
            when (progress) {
                is RestoreProgress.Finished -> inputs.update {
                    it.copy(
                        step = RestoreBackupUiState.Step.Finished,
                        restoreResult = progress.result,
                    )
                }

                is RestoreProgress.Restoring -> inputs.update {
                    it.copy(progress = progress, log = it.log.append(progress.currentFile))
                }

                else -> inputs.update { it.copy(progress = progress) }
            }
        }

        zipInputStream.close()
    }

    /** Logs every file once, keyed on the index, because file names can repeat in a vault. */
    private fun List<RestoreLogEntry>.append(
        currentFile: RestoreProgress.CurrentFile?,
    ): List<RestoreLogEntry> {
        currentFile ?: return this
        if (currentFile.index == lastOrNull()?.index) return this

        return (this + RestoreLogEntry(currentFile.index, currentFile.fileName))
            .takeLast(LOG_ENTRIES)
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted(RESTORE_BACKUP_URI) restoreBackupUri: Uri): RestoreBackupViewModel
    }
}
