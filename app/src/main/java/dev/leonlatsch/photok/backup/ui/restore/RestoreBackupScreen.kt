package dev.leonlatsch.photok.backup.ui.restore

import android.net.Uri
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
            is RestoreBackupUiState.Restoring -> Unit
        }
    }
}
