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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
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
                    Text(stringResource(R.string.backup_restore_restoring_title))
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

                    Text(
                        stringResource(
                            if (uiState.canceling) {
                                R.string.backup_restore_canceling
                            } else {
                                R.string.backup_restore_cancel
                            }
                        )
                    )
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
                subtitle = stringResource(R.string.backup_restore_restoring_subtitle),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(50.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(
                        R.string.backup_restore_restoring_percent,
                        (uiState.progress * 100).roundToInt(),
                    ),
                    style = MaterialTheme.typography.displayMedium,
                    fontFamily = FontFamily.Monospace,
                )

                Column(
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = stringResource(
                            R.string.backup_restore_restoring_bytes,
                            formatBytes(uiState.bytesDone),
                            formatBytes(uiState.bytesTotal),
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamily.Monospace,
                    )

                    Text(
                        text = stringResource(
                            R.string.backup_restore_restoring_speed,
                            formatBytes(uiState.bytesPerSecond),
                        ),
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
                    text = stringResource(
                        R.string.backup_restore_restoring_files,
                        uiState.filesDone,
                        uiState.filesTotal,
                    ),
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
                text = stringResource(R.string.backup_restore_restoring_keep_open),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun formatBytes(bytes: Long) = BindingConverters.formatByteSizeConverter(bytes)

@Composable
private fun formatTimeRemaining(millisRemaining: Long?): String {
    millisRemaining ?: return stringResource(R.string.backup_restore_restoring_estimating)

    val minutes = TimeUnit.MILLISECONDS.toMinutes(millisRemaining)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millisRemaining) % 60

    return when {
        minutes > 0 -> stringResource(
            R.string.backup_restore_restoring_time_left_minutes,
            minutes,
            seconds,
        )

        else -> stringResource(R.string.backup_restore_restoring_time_left_seconds, seconds)
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
