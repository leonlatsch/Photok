package dev.leonlatsch.photok.backup.ui.restore

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.databinding.BindingConverters
import dev.leonlatsch.photok.ui.theme.AppTheme
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreBackupRestoring(
    uiState: RestoreBackupUiState.Restoring,
    handleUiEvent: (RestoreBackupUiEvent) -> Unit,
) {
    // Leaving here would kill the restore and leave half written files behind. Use cancel instead.
    BackHandler {}

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Restoring Backup")
                },
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier.navigationBarsPadding()
            ) {
                RestoreLog(
                    entries = uiState.log,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { handleUiEvent(RestoreBackupUiEvent.CancelRestoreClicked) },
                    enabled = !uiState.canceling,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    if (uiState.canceling) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp),
                        )

                        Spacer(Modifier.size(10.dp))
                    }

                    Text(if (uiState.canceling) "Canceling restore…" else "Cancel restore")
                }
            }
        }
    ) { contentPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(contentPadding)
                .padding(20.dp)
        ) {
            BackupFileHeader(
                fileName = uiState.fileName,
                subtitle = "Restoring…",
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(50.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "${(uiState.progress * 100).roundToInt()}%",
                    style = MaterialTheme.typography.displayMedium,
                    fontFamily = FontFamily.Monospace,
                )

                Column(
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = "${formatBytes(uiState.bytesDone)} of ${formatBytes(uiState.bytesTotal)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamily.Monospace,
                    )

                    Text(
                        text = "${formatBytes(uiState.bytesPerSecond)}/s",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            LinearProgressIndicator(
                progress = { uiState.progress },
                modifier = Modifier
                    .height(8.dp)
                    .fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "${uiState.filesDone} / ${uiState.filesTotal} files",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )

                Text(
                    text = formatTimeRemaining(uiState.millisRemaining),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }

            Spacer(Modifier.height(50.dp))

            Text(
                text = "This can take a while. Please keep the app open.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun formatBytes(bytes: Long) = BindingConverters.formatByteSizeConverter(bytes)

private fun formatTimeRemaining(millisRemaining: Long?): String {
    millisRemaining ?: return "Estimating…"

    val minutes = TimeUnit.MILLISECONDS.toMinutes(millisRemaining)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millisRemaining) % 60

    return when {
        minutes > 0 -> "$minutes min $seconds s left"
        else -> "$seconds s left"
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    AppTheme {
        RestoreBackupRestoring(
            uiState = RestoreBackupUiState.Restoring(
                fileName = "photok_backup_1234.zip",
                filesDone = 42,
                filesTotal = 128,
                bytesDone = 420_000_000L,
                bytesTotal = 1_400_000_000L,
                bytesPerSecond = 12_000_000L,
                millisRemaining = 81_000L,
                log = listOf(
                    RestoreLogEntry(40, "VID_20240418_101233.mp4"),
                    RestoreLogEntry(41, "IMG_20240418_102907.jpg"),
                    RestoreLogEntry(42, "IMG_20240418_112238.jpg"),
                ),
                canceling = false,
            ),
            handleUiEvent = {},
        )
    }
}
