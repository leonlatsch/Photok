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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.backup.domain.FailedFile
import dev.leonlatsch.photok.ui.components.CenteredScrollableColumn
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.theme.Colors
import java.io.IOException
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreBackupFinished(
    uiState: RestoreBackupUiState.Finished,
    onDone: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.backup_restore_finished_title))
                },
            )
        },
        bottomBar = {
            Button(
                onClick = onDone,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.common_done))
                }
            }
        }
    ) { contentPadding ->
        if (uiState.failedFiles.isEmpty()) {
            RestoreBackupFinishedSuccess(
                uiState = uiState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
            )
        } else {
            RestoreBackupFinishedWithFailures(
                uiState = uiState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
            )
        }
    }
}

@Composable
private fun RestoreBackupFinishedSuccess(
    uiState: RestoreBackupUiState.Finished,
    modifier: Modifier = Modifier,
) {
    CenteredScrollableColumn(modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_check_circle_outline),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.backup_restore_finished_headline),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = summary(uiState),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.fillMaxWidth(),
        )

        SkippedLine(uiState)
    }
}

@Composable
private fun SkippedLine(uiState: RestoreBackupUiState.Finished) {
    if (uiState.filesSkipped == 0) return

    Spacer(Modifier.height(5.dp))

    val text = when (uiState.filesSkipped) {
        1 -> stringResource(R.string.backup_restore_finished_skipped_one)
        else -> stringResource(R.string.backup_restore_finished_skipped, uiState.filesSkipped)
    }

    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun RestoreBackupFinishedWithFailures(
    uiState: RestoreBackupUiState.Finished,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(20.dp)
    ) {
        Spacer(Modifier.height(30.dp))

        Icon(
            painter = painterResource(R.drawable.ic_warning),
            contentDescription = null,
            tint = Colors.Warning,
            modifier = Modifier.size(72.dp)
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = failuresHeadline(uiState.failedFiles.size),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = summary(uiState),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.fillMaxWidth(),
        )

        SkippedLine(uiState)

        Spacer(Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.backup_restore_finished_failed_items),
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(horizontal = 10.dp)
        )

        Spacer(Modifier.height(5.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(uiState.failedFiles) { failedFile ->
                FailedItemCard(
                    failedFile = failedFile,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun FailedItemCard(
    failedFile: FailedFile,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(10.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_warning),
                contentDescription = null,
                tint = Colors.Warning,
                modifier = Modifier.size(20.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = failedFile.fileName,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.MiddleEllipsis,
                )

                Text(
                    text = failedFile.cause?.localizedMessage
                        ?: stringResource(R.string.backup_restore_finished_unknown_error),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

@Composable
private fun failuresHeadline(failedCount: Int): String = when (failedCount) {
    1 -> stringResource(R.string.backup_restore_finished_headline_one_issue)
    else -> stringResource(R.string.backup_restore_finished_headline_issues, failedCount)
}

@Composable
private fun summary(uiState: RestoreBackupUiState.Finished): String {
    val duration = formatDuration(uiState.durationMillis)

    return if (uiState.albumsRestored > 0) {
        stringResource(
            R.string.backup_restore_finished_summary_with_albums,
            uiState.filesRestored,
            uiState.filesTotal,
            uiState.albumsRestored,
            duration,
        )
    } else {
        stringResource(
            R.string.backup_restore_finished_summary,
            uiState.filesRestored,
            uiState.filesTotal,
            duration,
        )
    }
}

@Composable
private fun formatDuration(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

    return when {
        minutes > 0 -> stringResource(
            R.string.backup_restore_finished_duration_minutes,
            minutes,
            seconds,
        )

        else -> stringResource(R.string.backup_restore_finished_duration_seconds, seconds)
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    AppTheme {
        RestoreBackupFinished(
            uiState = RestoreBackupUiState.Finished(
                fileName = "photok_backup_1234.zip",
                filesRestored = 128,
                filesTotal = 128,
                filesSkipped = 0,
                albumsRestored = 4,
                durationMillis = 134_000L,
                failedFiles = emptyList(),
            ),
            onDone = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewWithFailures() {
    AppTheme {
        RestoreBackupFinished(
            uiState = RestoreBackupUiState.Finished(
                fileName = "photok_backup_1234.zip",
                filesRestored = 125,
                filesTotal = 128,
                filesSkipped = 0,
                albumsRestored = 4,
                durationMillis = 134_000L,
                failedFiles = listOf(
                    FailedFile(
                        fileName = "IMG_20240418_102907.jpg",
                        cause = IOException("Unexpected end of stream"),
                    ),
                    FailedFile(
                        fileName = "VID_20240418_101233.mp4",
                        cause = IOException("No space left on device"),
                    ),
                    FailedFile(
                        fileName = "IMG_20240418_112238.jpg",
                        cause = null,
                    ),
                    FailedFile(
                        fileName = "IMG_20240418_112238.jpg",
                        cause = null,
                    ),
                    FailedFile(
                        fileName = "IMG_20240418_112238.jpg",
                        cause = null,
                    ),
                    FailedFile(
                        fileName = "IMG_20240418_112238.jpg",
                        cause = null,
                    ),
                    FailedFile(
                        fileName = "IMG_20240418_112238.jpg",
                        cause = null,
                    ),
                    FailedFile(
                        fileName = "IMG_20240418_112238.jpg",
                        cause = null,
                    ),
                    FailedFile(
                        fileName = "IMG_20240418_112238.jpg",
                        cause = null,
                    ),
                    FailedFile(
                        fileName = "IMG_20240418_112238.jpg",
                        cause = null,
                    ),
                ),
            ),
            onDone = {},
        )
    }
}
