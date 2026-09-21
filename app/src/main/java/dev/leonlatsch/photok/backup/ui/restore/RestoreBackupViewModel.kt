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
import dev.leonlatsch.photok.backup.domain.FailedFile
import dev.leonlatsch.photok.backup.domain.RestoreBackupStrategy
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
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

const val RESTORE_BACKUP_URI = "restore_backup_uri"

private const val LOG_ENTRIES = RESTORE_LOG_ROWS

// Fast indexing looks weird in UI. Keep screen active for at least this time
private val MinIndexingDuration = 2.seconds.inWholeMilliseconds

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
        val progress: Float =
            if (bytesTotal == 0L) 0f else bytesDone.toFloat() / bytesTotal.toFloat()
    }

    data class Indexing(
        val fileName: String,
    ) : RestoreBackupUiState

    data class Finished(
        val fileName: String,
        val filesRestored: Int,
        val filesTotal: Int,
        val albumsRestored: Int,
        val durationMillis: Long,
        val failedFiles: List<FailedFile>,
    ) : RestoreBackupUiState

    data class Canceled(
        val fileName: String,
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

    private var indexingStartedAt = 0L

    private var restoreJob: Job? = null

    private var activeStrategy: RestoreBackupStrategy<*>? = null

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
                wrongPassword = inputs.wrongPassword,
            )

            inputs.step == RestoreBackupUiState.Step.Restoring ->
                restoringState(inputs.progress, inputs.log, inputs.canceling)

            inputs.step == RestoreBackupUiState.Step.Canceled ->
                RestoreBackupUiState.Canceled(fileName = fileName)

            inputs.step == RestoreBackupUiState.Step.Finished && inputs.restoreResult != null ->
                finishedState(inputs.restoreResult)

            else -> RestoreBackupUiState.Overview(
                validation = validation,
                emptyVault = emptyVault,
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

            is RestoreBackupUiEvent.ConfirmPasswordClicked -> {
                restoreJob = unlockAndRestore()
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
        val metaData = validation.value?.metaData ?: return@launch
        val password = inputs.value.password

        inputs.update { it.copy(unlocking = true, wrongPassword = false) }

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
        val strategy = activeStrategy ?: return@launch

        inputs.update { it.copy(canceling = true) }

        restoreJob?.cancelAndJoin()
        restoreJob = null

        strategy.abort()
        activeStrategy = null

        inputs.update {
            it.copy(step = RestoreBackupUiState.Step.Canceled, canceling = false)
        }
    }

    private suspend fun restoreBackup(metaData: BackupMetaData, session: Session) {
        val zipInputStream = io.zip.openZipInput(restoreBackupUri)

        val progressFlow = when (metaData) {
            is BackupMetaData.V1 -> {
                activeStrategy = v1Strategy
                v1Strategy.restore(metaData, zipInputStream, session)
            }

            is BackupMetaData.V2 -> {
                activeStrategy = v2Strategy
                v2Strategy.restore(metaData, zipInputStream, session)
            }

            is BackupMetaData.V3 -> {
                activeStrategy = v3Strategy
                v3Strategy.restore(metaData, zipInputStream, session)
            }

            is BackupMetaData.V4 -> {
                activeStrategy = v4Strategy
                v4Strategy.restore(metaData, zipInputStream, session)
            }

            is BackupMetaData.V5 -> {
                activeStrategy = v5Strategy
                v5Strategy.restore(metaData, zipInputStream, session)
            }
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
            .takeLast(LOG_ENTRIES)
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted(RESTORE_BACKUP_URI) restoreBackupUri: Uri): RestoreBackupViewModel
    }
}
