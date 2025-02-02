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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastJoinToString
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.theme.AppTheme
import timber.log.Timber

private val EnterAnimSpec = scaleIn(
    transformOrigin = TransformOrigin(1f, 1f)
) + fadeIn()

private val ExitAnimSpec = scaleOut(
    transformOrigin = TransformOrigin(1f, 1f)
) + fadeOut()

@Composable
fun MagicFabMenu(
    openState: MutableState<Boolean>,
    modifier: Modifier = Modifier,
) {
    val importNewFilesLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) {
            openState.value = false
            Timber.d(it.fastJoinToString(", "))
        }
    val restoreBackupLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
            Timber.d(it.toString())
        }

    AnimatedVisibility(
        visible = openState.value,
        enter = EnterAnimSpec,
        exit = ExitAnimSpec,
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { openState.value = false },
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorResource(R.color.cardBackground))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MagicFabMenuItem(
                    menuOpenState = openState,
                    title = "Import new Files",
                    description = "Import files from your gallery.",
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_add),
                            contentDescription = null,
                            tint = colorResource(R.color.textColor),
                        )
                    },
                    onClick = {
                        importNewFilesLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                    },
                )
                MagicFabMenuItem(
                    menuOpenState = openState,
                    title = "Restore backup",
                    description = "Restore a backup. The restored backup will be added to your current files.",
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_backup_restore),
                            contentDescription = null,
                            tint = colorResource(R.color.colorComplementary),
                        )
                    },
                    onClick = {
                        restoreBackupLauncher.launch(arrayOf("application/zip"))
                    },
                )
            }
        }
    }
}

@Composable
private fun MagicFabMenuItem(
    menuOpenState: MutableState<Boolean>,
    title: String,
    description: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.clickable(role = Role.Button) {
            menuOpenState.value = false
            onClick()
        },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        icon()

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.textColor),
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.secondaryTextColor),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    val openState = remember { mutableStateOf(true) }
    AppTheme {
        Surface {
            MagicFabMenu(openState)
        }
    }
}