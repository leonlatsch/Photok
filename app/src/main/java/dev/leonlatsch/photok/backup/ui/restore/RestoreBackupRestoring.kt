package dev.leonlatsch.photok.backup.ui.restore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.databinding.BindingConverters
import dev.leonlatsch.photok.ui.theme.AppTheme
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreBackupRestoring(
    uiState: RestoreBackupUiState.Restoring,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Restoring Backup")
                },
            )
        }
    ) { contentPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(20.dp)
        ) {
            CircularProgressIndicator(
                progress = { uiState.progress },
                modifier = Modifier.size(64.dp),
                strokeWidth = 6.dp,
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Restoring Backup",
                style = MaterialTheme.typography.headlineMedium,
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = uiState.fileName,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(20.dp))

            LinearProgressIndicator(
                progress = { uiState.progress },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "${uiState.filesDone} / ${uiState.filesTotal} files",
                    color = MaterialTheme.colorScheme.outline,
                )

                Text(
                    text = "${formatBytes(uiState.bytesDone)} / ${formatBytes(uiState.bytesTotal)}",
                    color = MaterialTheme.colorScheme.outline,
                )
            }

            Spacer(Modifier.height(5.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "${formatBytes(uiState.bytesPerSecond)}/s",
                    color = MaterialTheme.colorScheme.outline,
                )

                Text(
                    text = formatTimeRemaining(uiState.millisRemaining),
                    color = MaterialTheme.colorScheme.outline,
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "This can take a while. Please keep the app open.",
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
            ),
        )
    }
}
