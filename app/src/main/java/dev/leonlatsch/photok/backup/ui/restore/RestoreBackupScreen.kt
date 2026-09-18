package dev.leonlatsch.photok.backup.ui.restore

import android.net.Uri
import androidx.compose.material3.CircularProgressIndicator
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
            is RestoreBackupUiState.Validating -> CircularProgressIndicator()
            is RestoreBackupUiState.Overview -> RestoreBackupOverview(
                uiState = state,
                onClose = onClose,
            )
            is RestoreBackupUiState.Unlock -> Unit
            is RestoreBackupUiState.Restoring -> Unit
        }
    }
}
