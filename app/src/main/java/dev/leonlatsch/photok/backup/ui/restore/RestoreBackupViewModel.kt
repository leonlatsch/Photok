/*
 *   Copyright 2020–2026 Leon Latsch
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

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
import dev.leonlatsch.photok.backup.domain.BackupValidationError
import dev.leonlatsch.photok.backup.domain.FailedFile
import dev.leonlatsch.photok.backup.domain.RestoreBackupRunner
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
import dev.leonlatsch.photok.model.repositories.CleanupDeadFilesUseCase
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.review.InAppReview
import dev.leonlatsch.photok.review.ReviewTrigger
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

const val RESTORE_BACKUP_URI = "restore_backup_uri"

// Fast indexing looks weird in UI. Keep screen active for at least this time
private val MinIndexingDuration = 2.seconds.inWholeMilliseconds

sealed interface RestoreBackupUiState {
    data class Validating(
        val fileName: String,
    ) : RestoreBackupUiState

    data class Overview(
        val validation: BackupValidation,
        val emptyVault: Boolean,
        val duplicateHandling: DuplicateHandling,
    ) : RestoreBackupUiState

    data class Unlock(
        val fileName: String,
        val password: String,
        val unlocking: Boolean,
        val wrongPassword: Boolean,
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
        val canceling: Boolean,
    ) : RestoreBackupUiState {
        /**
         * Counted in bytes, or in files when the declared sizes are unusable. Old backups
         * declare a size of 0 for some photos, which would pin the bar at 0% for the whole
         * restore. Same fallback the remaining time estimate uses.
         */
        val progress: Float = when {
            bytesTotal > 0L -> bytesDone.toFloat() / bytesTotal.toFloat()
            filesTotal > 0 -> filesDone.toFloat() / filesTotal.toFloat()
            else -> 0f
        }
    }

    data class Indexing(
        val fileName: String,
    ) : RestoreBackupUiState

    data class Finished(
        val fileName: String,
        val filesRestored: Int,
        val filesTotal: Int,
        val filesSkipped: Int,
        val albumsRestored: Int,
        val durationMillis: Long,
        val failedFiles: List<FailedFile>,
    ) : RestoreBackupUiState

    data class Canceled(
        val fileName: String,
    ) : RestoreBackupUiState

    data class ValidationFailed(
        val fileName: String,
        val error: BackupValidationError,
    ) : RestoreBackupUiState

    enum class Step {
        Overview,
        Unlock,
        Restoring,
        Finished,
        Canceled,
    }

    data class Inputs(
        val step: Step = Step.Overview,
        val password: String = "",
        val unlocking: Boolean = false,
        val wrongPassword: Boolean = false,
        val canceling: Boolean = false,
        val duplicateHandling: DuplicateHandling = DuplicateHandling.Skip,
        val progress: RestoreProgress? = null,
        val log: List<RestoreLogEntry> = emptyList(),
        val restoreResult: RestoreResult? = null,
    )
}

private sealed interface ValidationState {
    data object Validating : ValidationState
    data class Valid(val validation: BackupValidation) : ValidationState
    data class Invalid(val error: BackupValidationError) : ValidationState
}

@HiltViewModel(assistedFactory = RestoreBackupViewModel.Factory::class)
class RestoreBackupViewModel @AssistedInject constructor(
    @Assisted(RESTORE_BACKUP_URI) private val restoreBackupUri: Uri,
    private val validateBackupUseCase: ValidateBackupUseCase,
    private var unlockBackupUseCase: UnlockBackupUseCase,
    private val photoRepository: PhotoRepository,
    private val io: IO,
    private val inAppReview: InAppReview,
    private val cleanupDeadFiles: CleanupDeadFilesUseCase,
    private val runner: RestoreBackupRunner,
    private val v1Strategy: RestoreBackupV1,
    private val v2Strategy: RestoreBackupV2,
    private val v3Strategy: RestoreBackupV3,
    private val v4Strategy: RestoreBackupV4,
    private val v5Strategy: RestoreBackupV5,
) : ViewModel() {

    private val fileName = io.getFileName(restoreBackupUri).orEmpty()

    private var indexingStartedAt = 0L

    private var restoreJob: Job? = null

    private val inputs = MutableStateFlow(RestoreBackupUiState.Inputs())

    private val validation = flow {
        val state = validateBackupUseCase(restoreBackupUri).fold(
            onSuccess = { ValidationState.Valid(it) },
            onFailure = {
                val error = it as? BackupValidationError ?: BackupValidationError.Unknown(it)
                ValidationState.Invalid(error)
            },
        )

        emit(state)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ValidationState.Validating)

    /** The archive validated, but could not be opened again when the restore started. */
    private val reopenFailed = MutableStateFlow(false)

    private val emptyVault = flow {
        emit(photoRepository.countAll() == 0)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val validatingState = RestoreBackupUiState.Validating(fileName = fileName)

    val uiState = combine(
        inputs,
        validation,
        emptyVault,
        reopenFailed,
    ) { inputs, validation, emptyVault, reopenFailed ->
        if (reopenFailed) {
            return@combine RestoreBackupUiState.ValidationFailed(
                fileName = fileName,
                error = BackupValidationError.CannotOpenFile(),
            )
        }

        val backup = when (validation) {
            is ValidationState.Validating -> return@combine validatingState

            is ValidationState.Invalid -> return@combine RestoreBackupUiState.ValidationFailed(
                fileName = fileName,
                error = validation.error,
            )

            is ValidationState.Valid -> validation.validation
        }

        when (inputs.step) {
            RestoreBackupUiState.Step.Unlock -> RestoreBackupUiState.Unlock(
                fileName = fileName,
                password = inputs.password,
                unlocking = inputs.unlocking,
                wrongPassword = inputs.wrongPassword,
            )

            RestoreBackupUiState.Step.Restoring ->
                restoringState(inputs.progress, inputs.log, inputs.canceling)

            RestoreBackupUiState.Step.Canceled ->
                RestoreBackupUiState.Canceled(fileName = fileName)

            RestoreBackupUiState.Step.Finished if inputs.restoreResult != null ->
                finishedState(inputs.restoreResult)

            else -> RestoreBackupUiState.Overview(
                validation = backup,
                emptyVault = emptyVault,
                duplicateHandling = inputs.duplicateHandling,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), validatingState)

    private fun restoringState(
        progress: RestoreProgress?,
        log: List<RestoreLogEntry>,
        canceling: Boolean,
    ): RestoreBackupUiState =
        when (progress) {
            is RestoreProgress.Indexing -> RestoreBackupUiState.Indexing(fileName = fileName)

            is RestoreProgress.Restoring -> RestoreBackupUiState.Restoring(
                fileName = fileName,
                filesDone = progress.filesDone,
                filesTotal = progress.filesTotal,
                bytesDone = progress.bytesDone,
                bytesTotal = progress.bytesTotal,
                bytesPerSecond = progress.bytesPerSecond,
                millisRemaining = progress.millisRemaining,
                log = log,
                canceling = canceling,
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
                canceling = canceling,
            )
        }

    private fun finishedState(result: RestoreResult) = RestoreBackupUiState.Finished(
        fileName = fileName,
        filesRestored = result.filesRestored,
        filesTotal = result.filesTotal,
        filesSkipped = result.filesSkipped,
        albumsRestored = result.albumsRestored,
        durationMillis = result.durationMillis,
        failedFiles = result.failedFiles,
    )

    fun handleUiEvent(event: RestoreBackupUiEvent) {
        when (event) {
            is RestoreBackupUiEvent.UnlockBackupClicked -> inputs.update {
                it.copy(step = RestoreBackupUiState.Step.Unlock)
            }

            is RestoreBackupUiEvent.BackToOverviewClicked -> inputs.update {
                it.copy(
                    step = RestoreBackupUiState.Step.Overview,
                    password = "",
                    wrongPassword = false,
                )
            }

            is RestoreBackupUiEvent.PasswordChanged -> inputs.update {
                it.copy(password = event.password, wrongPassword = false)
            }

            // The keyboard's done action fires next to the button, guard against a second restore
            is RestoreBackupUiEvent.ConfirmPasswordClicked -> {
                if (!inputs.value.unlocking) {
                    inputs.update { it.copy(unlocking = true, wrongPassword = false) }
                    restoreJob = unlockAndRestore()
                }
            }

            is RestoreBackupUiEvent.DuplicateHandlingChanged -> inputs.update {
                it.copy(duplicateHandling = event.duplicateHandling)
            }

            is RestoreBackupUiEvent.CancelRestoreClicked -> cancelRestore()

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
        val metaData = (validation.value as? ValidationState.Valid)?.validation?.metaData
        if (metaData == null) {
            inputs.update { it.copy(unlocking = false) }
            return@launch
        }

        val password = inputs.value.password

        unlockBackupUseCase(restoreBackupUri, metaData, password)
            .onSuccess { session ->
                inputs.update {
                    it.copy(
                        step = RestoreBackupUiState.Step.Restoring,
                        password = "",
                        unlocking = false,
                        wrongPassword = false,
                    )
                }

                restoreBackup(metaData, session)
            }
            .onFailure {
                inputs.update { it.copy(unlocking = false, wrongPassword = true) }
            }
    }

    private fun cancelRestore() = viewModelScope.launch {
        if (inputs.value.canceling) return@launch

        inputs.update { it.copy(canceling = true) }

        restoreJob?.cancelAndJoin()
        restoreJob = null

        // Aborting both is fine, only the one that ran has anything to clean up.
        v1Strategy.abort()
        runner.abort()

        inputs.update {
            it.copy(step = RestoreBackupUiState.Step.Canceled, canceling = false)
        }
    }

    private suspend fun restoreBackup(metaData: BackupMetaData, session: Session) {
        val zipInputStream = io.zip.openZipInput(restoreBackupUri)
        if (zipInputStream == null) {
            Timber.e("Could not open backup for restoring: $restoreBackupUri")
            reopenFailed.value = true
            return
        }

        val skipUuids = getUUIDsToSkip(metaData)

        // V1 stands on its own: it has no thumbnails in the archive and regenerates them from
        // the decoded image. Every later format goes through the runner.
        val progressFlow = when (metaData) {
            is BackupMetaData.V1 ->
                v1Strategy.restore(metaData, zipInputStream, session, skipUuids)

            is BackupMetaData.V2 ->
                runner.run(v2Strategy, metaData, zipInputStream, session, skipUuids)

            is BackupMetaData.V3 ->
                runner.run(v3Strategy, metaData, zipInputStream, session, skipUuids)

            is BackupMetaData.V4 ->
                runner.run(v4Strategy, metaData, zipInputStream, session, skipUuids)

            is BackupMetaData.V5 ->
                runner.run(v5Strategy, metaData, zipInputStream, session, skipUuids)
        }

        zipInputStream.use { zipInputStream ->
            progressFlow.collect { progress ->
                when (progress) {
                    is RestoreProgress.Restoring -> inputs.update {
                        it.copy(progress = progress, log = it.log.append(progress.currentFile))
                    }

                    is RestoreProgress.Indexing -> {
                        indexingStartedAt = System.currentTimeMillis()
                        inputs.update { it.copy(progress = progress) }
                    }

                    is RestoreProgress.Finished -> {
                        // A failed file can leave a half written file behind with no row
                        // pointing at it.
                        if (progress.result.failedFiles.isNotEmpty()) {
                            cleanupDeadFiles()
                        }

                        awaitMinimumIndexingTime()

                        inputs.update {
                            it.copy(
                                step = RestoreBackupUiState.Step.Finished,
                                restoreResult = progress.result,
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun getUUIDsToSkip(metaData: BackupMetaData): Set<String> {
        if (inputs.value.duplicateHandling == DuplicateHandling.Replace) return emptySet()

        val vaultUuids = photoRepository.getAllUuids().toSet()
        return metaData.photos.map { it.uuid }.filter { it in vaultUuids }.toSet()
    }

    private suspend fun awaitMinimumIndexingTime() {
        val elapsed = System.currentTimeMillis() - indexingStartedAt
        val remaining = MinIndexingDuration - elapsed

        if (remaining > 0) delay(remaining.milliseconds)
    }

    /** Logs every file once, keyed on the index, because file names can repeat in a vault. */
    private fun List<RestoreLogEntry>.append(
        currentFile: RestoreProgress.CurrentFile?,
    ): List<RestoreLogEntry> {
        currentFile ?: return this
        if (currentFile.index == lastOrNull()?.index) return this

        return (this + RestoreLogEntry(currentFile.index, currentFile.fileName))
            .takeLast(RESTORE_LOG_ROWS)
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted(RESTORE_BACKUP_URI) restoreBackupUri: Uri): RestoreBackupViewModel
    }
}
