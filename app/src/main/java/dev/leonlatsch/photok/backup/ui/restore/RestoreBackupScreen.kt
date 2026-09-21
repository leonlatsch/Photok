package dev.leonlatsch.photok.backup.ui.restore

import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

        when (val state = uiState) {
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
        }
    }
}
