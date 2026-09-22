package dev.leonlatsch.photok.backup.ui.restore

import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.ui.animation.slideBackward
import dev.leonlatsch.photok.ui.animation.slideForward
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun RestoreBackupScreen(
    backupUri: Uri,
    onClose: () -> Unit,
) {
    AppTheme {
        val viewModel: RestoreBackupViewModel =
            hiltViewModel<RestoreBackupViewModel, RestoreBackupViewModel.Factory>(
                creationCallback = { factory ->
                    factory.create(backupUri)
                }
            )

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val activity = LocalActivity.current

        // Keying on the step position keeps a fast updating screen like restoring from restarting
        // the animation on every progress update, and leaves the screen sliding out rendering the
        // last state that belonged to it instead of the state of the screen sliding in.
        AnimatedContent(
            targetState = uiState,
            contentKey = { it.stepPosition },
            transitionSpec = {
                if (targetState.stepPosition > initialState.stepPosition) {
                    slideForward()
                } else {
                    slideBackward()
                }
            },
            modifier = Modifier.fillMaxSize(),
        ) { state ->
            when (state) {
                is RestoreBackupUiState.Validating -> RestoreBackupOverviewLoading(
                    uiState = state,
                    onClose = onClose,
                )
                is RestoreBackupUiState.Overview -> RestoreBackupOverview(
                    uiState = state,
                    handleUiEvent = viewModel::handleUiEvent,
                    onClose = onClose,
                )
                is RestoreBackupUiState.Unlock -> RestoreBackupUnlock(
                    uiState = state,
                    handleUiEvent = viewModel::handleUiEvent,
                )
                is RestoreBackupUiState.Restoring -> RestoreBackupRestoring(
                    uiState = state,
                    handleUiEvent = viewModel::handleUiEvent,
                )
                is RestoreBackupUiState.Indexing -> RestoreBackupIndexing(
                    uiState = state,
                )
                is RestoreBackupUiState.Finished -> RestoreBackupFinished(
                    uiState = state,
                    onDone = {
                        viewModel.handleUiEvent(RestoreBackupUiEvent.DoneClicked(activity))
                        onClose()
                    },
                )
                is RestoreBackupUiState.Canceled -> RestoreBackupCanceled(
                    uiState = state,
                    onDone = onClose,
                )
            }
        }
    }
}

/**
 * Position of a state in the restore flow. It drives the slide direction and stays stable while a
 * single screen updates, so a fast updating screen like restoring does not restart the animation.
 *
 * Validating and Overview share a position on purpose: the loading overview turns into the real
 * overview without any animation.
 */
private val RestoreBackupUiState.stepPosition: Int
    get() = when (this) {
        is RestoreBackupUiState.Validating -> 0
        is RestoreBackupUiState.Overview -> 0
        is RestoreBackupUiState.Unlock -> 1
        is RestoreBackupUiState.Restoring -> 2
        is RestoreBackupUiState.Indexing -> 3
        is RestoreBackupUiState.Finished -> 4
        is RestoreBackupUiState.Canceled -> 4
    }
