/*
 *   Copyright 2020-2024 Leon Latsch
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

package dev.leonlatsch.photok.gallery.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.settings.ui.compose.LocalConfig
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.theme.Colors

sealed interface ImportChoice {
    data class AddNewFiles(val fileUris: List<Uri>) : ImportChoice
    data class RestoreBackup(val backupUri: Uri) : ImportChoice
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportMenuBottomSheet(
    openState: MutableState<Boolean>,
    onImportChoice: (ImportChoice) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (openState.value) {
        ModalBottomSheet(
            modifier = modifier,
            onDismissRequest = { openState.value = false },
            dragHandle = {
                BottomSheetDefaults.DragHandle()
            }
        ) {
            ImportMenuDialogContent(
                onImportChoice = onImportChoice,
                openState = openState,
            )
        }
    }
}

@Composable
private fun ImportMenuDialogContent(
    openState: MutableState<Boolean>,
    onImportChoice: (ImportChoice) -> Unit,
    modifier: Modifier = Modifier
) {
    val importNewFilesLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) {
            openState.value = false
            it.ifEmpty { return@rememberLauncherForActivityResult }

            onImportChoice(
                ImportChoice.AddNewFiles(fileUris = it)
            )

        }
    val restoreBackupLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
            openState.value = false
            it ?: return@rememberLauncherForActivityResult

            onImportChoice(
                ImportChoice.RestoreBackup(backupUri = it)
            )
        }

    val config = LocalConfig.current
    val isPreview = LocalInspectionMode.current

    Column(
        modifier = modifier.padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ImportMenuItem(
            text = stringResource(R.string.import_menu_new_files_title),
            description = stringResource(R.string.import_menu_new_files_description),
            iconPainter = painterResource(R.drawable.ic_add),
            chips = { modifier ->

                val showWarningChip =
                    config?.deleteImportedFiles == true || isPreview

                if (showWarningChip) {
                    ImportWarningChip(modifier = modifier)
                }
            },
            onClick = {
                importNewFilesLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                )
            }
        )

        HorizontalDivider()

        ImportMenuItem(
            text = stringResource(R.string.import_menu_restore_title),
            description = stringResource(R.string.import_menu_restore_description),
            iconPainter = painterResource(R.drawable.ic_backup_restore),
            onClick = {
                restoreBackupLauncher.launch(arrayOf("application/zip"))
            }
        )
    }
}

@Composable
fun ImportWarningChip(modifier: Modifier = Modifier) {
    AssistChip(
        modifier = modifier,
        onClick = {},
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_warning),
                contentDescription = null,
            )
        },
        label = {
            Text(stringResource(R.string.import_menu_delete_warning))
        },
        colors = AssistChipDefaults.assistChipColors(
            leadingIconContentColor = Colors.Warning,
            labelColor = Colors.Warning,
        ),
    )
}

val IconSize = 24.dp
val IconEndPadding = 4.dp
val StartPadding = IconSize + IconEndPadding

@Composable
fun ImportMenuItem(
    text: String,
    description: String,
    iconPainter: Painter,
    onClick: () -> Unit,
    chips: @Composable (Modifier) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(IconSize),
                painter = iconPainter,
                contentDescription = null,
            )

            Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall,
            )
        }

        Text(
            modifier = Modifier.padding(start = StartPadding),
            text = description,
            style = MaterialTheme.typography.bodyMedium,
        )

        chips(Modifier.padding(start = StartPadding))
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    val openState = remember { mutableStateOf(true) }
    AppTheme {
        Surface {
            ImportMenuDialogContent(
                openState = openState,
                onImportChoice = {}
            )
        }
    }
}