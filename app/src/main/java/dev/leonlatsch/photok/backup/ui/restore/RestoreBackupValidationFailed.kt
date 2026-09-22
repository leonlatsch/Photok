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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.backup.domain.BackupValidationError
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.theme.Colors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreBackupValidationFailed(
    uiState: RestoreBackupUiState.ValidationFailed,
    onClose: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Restore Backup")
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = onClose,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    ) { contentPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(contentPadding)
                .padding(20.dp)
        ) {
            BackupFileHeader(
                fileName = uiState.fileName,
                subtitle = "Invalid backup",
            )

            Spacer(Modifier.height(40.dp))

            Icon(
                painter = painterResource(R.drawable.ic_warning),
                contentDescription = null,
                tint = Colors.Warning,
                modifier = Modifier.size(72.dp)
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = headline(uiState.error),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = description(uiState.error),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun headline(error: BackupValidationError) = when (error) {
    is BackupValidationError.CannotOpenFile -> "Backup can not be opened"
    is BackupValidationError.NoBackupFiles -> "No photos in this backup"
    is BackupValidationError.NoMetaData -> "Backup is incomplete"
    is BackupValidationError.Unknown -> "Backup can not be read"
}

private fun description(error: BackupValidationError) = when (error) {
    is BackupValidationError.CannotOpenFile ->
        "This file could not be opened. Make sure it is a Photok backup and still available on " +
            "this device."

    is BackupValidationError.NoBackupFiles ->
        "This archive does not contain any Photok files, so there is nothing to restore."

    is BackupValidationError.NoMetaData ->
        "This backup has no metadata, so Photok can not tell what is inside it."

    is BackupValidationError.Unknown ->
        "Something went wrong while reading this backup."
}

@PreviewLightDark
@Composable
private fun PreviewCannotOpenFile() {
    AppTheme {
        RestoreBackupValidationFailed(
            uiState = RestoreBackupUiState.ValidationFailed(
                fileName = "photok_backup_1234.zip",
                error = BackupValidationError.CannotOpenFile(),
            ),
            onClose = {},
        )
    }
}
