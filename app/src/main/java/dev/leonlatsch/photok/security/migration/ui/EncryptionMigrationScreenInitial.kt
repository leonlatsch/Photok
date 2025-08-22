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

package dev.leonlatsch.photok.security.migration.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.backup.domain.BackupStrategy
import dev.leonlatsch.photok.backup.ui.BackupBottomSheetDialogFragment
import dev.leonlatsch.photok.other.areNotificationsEnabled
import dev.leonlatsch.photok.other.extensions.launchAndIgnoreTimer
import dev.leonlatsch.photok.other.extensions.show
import dev.leonlatsch.photok.other.openNotificationSettings
import dev.leonlatsch.photok.other.requestInSettings
import dev.leonlatsch.photok.security.migration.ui.LegacyEncryptionMigrationUiEvent.StartMigration
import dev.leonlatsch.photok.security.migration.ui.LegacyEncryptionMigrationUiEvent.SwitchStage
import dev.leonlatsch.photok.ui.components.AppName
import dev.leonlatsch.photok.ui.theme.AppTheme


@Composable
fun EncryptionMigrationScreenInitial(
    uiState: LegacyEncryptionMigrationUiState.Initial,
    handleUiEvent: (LegacyEncryptionMigrationUiEvent) -> Unit,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateAsState()

    LaunchedEffect(lifecycleState) {
        if (lifecycleState == Lifecycle.State.RESUMED) {
            if (context.areNotificationsEnabled() && uiState.stage == InitialSubStage.PERMISSION) {
                handleUiEvent(SwitchStage(InitialSubStage.READY))
            }
        }
    }

    val createBackupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) {
        it ?: return@rememberLauncherForActivityResult
        if (activity !is AppCompatActivity) return@rememberLauncherForActivityResult

        BackupBottomSheetDialogFragment(
            uri = it,
            strategy = BackupStrategy.Name.Legacy,
        ).show(activity.supportFragmentManager)

        if (context.areNotificationsEnabled()) {
            handleUiEvent(SwitchStage(InitialSubStage.READY))
        } else {
            handleUiEvent(SwitchStage(InitialSubStage.PERMISSION))
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            handleUiEvent(SwitchStage(InitialSubStage.READY))
        }
    }

    Scaffold { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 12.dp),
        ) {

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                AnimatedVisibility(uiState.stage == InitialSubStage.INITIAL) {
                    Text(
                        text = "Welcome to"
                    )
                }

                Spacer(Modifier.height(24.dp))


                AppName()

                Spacer(Modifier.height(24.dp))

                AnimatedVisibility(uiState.stage == InitialSubStage.INITIAL) {
                    Text(
                        text = "This Version of Photok introduces a new encryption method. In order to continue to your gallery, there is a migration required.",
                        textAlign = TextAlign.Center,
                    )
                }

                AnimatedVisibility(uiState.stage != InitialSubStage.INITIAL) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AnimatedContent(
                            targetState = uiState.stage.value,
                            transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                        ) {
                            val icon: Painter
                            val color: Color

                            if (it > InitialSubStage.BACKUP.value) {
                                icon = painterResource(R.drawable.ic_check)
                                color = MaterialTheme.colorScheme.primary
                            } else {
                                icon = painterResource(R.drawable.ic_close)
                                color = MaterialTheme.colorScheme.outline
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    painter = icon,
                                    contentDescription = null,
                                    tint = color,
                                )
                                Text(
                                    text = "Backup your data",
                                    color = color,
                                )
                            }
                        }

                        AnimatedContent(
                            targetState = uiState.stage.value,
                            transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                        ) {
                            val icon: Painter
                            val color: Color

                            if (it > InitialSubStage.PERMISSION.value) {
                                icon = painterResource(R.drawable.ic_check)
                                color = MaterialTheme.colorScheme.primary
                            } else {
                                icon = painterResource(R.drawable.ic_close)
                                color = MaterialTheme.colorScheme.outline
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    painter = icon,
                                    contentDescription = null,
                                    tint = color,
                                )
                                Text(
                                    text = "Grant permissions",
                                    color = color,
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                AnimatedContent(
                    targetState = uiState.stage,
                    transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                ) {
                    val text = when (it) {
                        InitialSubStage.INITIAL -> null
                        InitialSubStage.BACKUP -> "Create a backup of your data before continuing"
                        InitialSubStage.PERMISSION -> "Please grant notification permissions in order to run the migration in the background"
                        InitialSubStage.READY -> "Youre all set"
                    }

                    if (text != null) {
                        Text(
                            text = text,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))



                AnimatedContent(
                    targetState = uiState.stage,
                    transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                ) {
                    when (it) {
                        InitialSubStage.INITIAL -> Button(
                            modifier = Modifier.defaultMinSize(minWidth = 200.dp),
                            onClick = {
                                handleUiEvent(
                                    SwitchStage(
                                        InitialSubStage.BACKUP
                                    )
                                )
                            }
                        ) {
                            Text("Get started")
                        }

                        InitialSubStage.BACKUP -> Button(
                            modifier = Modifier.defaultMinSize(minWidth = 200.dp),
                            onClick = {
                                // TODO: TEST CODE
                                handleUiEvent(SwitchStage(InitialSubStage.PERMISSION))
//                                createBackupLauncher.launchAndIgnoreTimer(
//                                    input = createBackupFilename(),
//                                    activity = activity,
//                                )
                            }
                        ) {
                            Text("Create backup")
                        }

                        InitialSubStage.PERMISSION -> Button(
                            modifier = Modifier.defaultMinSize(minWidth = 200.dp),
                            onClick = {
                                activity ?: return@Button

                                if (activity.requestInSettings(Manifest.permission.POST_NOTIFICATIONS)) {
                                    activity.openNotificationSettings()
                                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launchAndIgnoreTimer(Manifest.permission.POST_NOTIFICATIONS, activity)
                                } else {
                                    handleUiEvent(SwitchStage(InitialSubStage.READY))
                                }
                            }
                        ) {
                            Text("Grant permissions")
                        }

                        InitialSubStage.READY -> Button(
                            modifier = Modifier.defaultMinSize(minWidth = 200.dp),
                            onClick = {
                                handleUiEvent(
                                    StartMigration(
                                        context
                                    )
                                )
                            }
                        ) {
                            Text("Start migration")
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewInitial() {
    AppTheme {
        EncryptionMigrationScreenInitial(
            uiState = LegacyEncryptionMigrationUiState.Initial(),
            handleUiEvent = {},
        )
    }
}

@Preview
@Composable
private fun PreviewInitialBackpup() {
    AppTheme {
        EncryptionMigrationScreenInitial(
            uiState = LegacyEncryptionMigrationUiState.Initial(stage = InitialSubStage.BACKUP),
            handleUiEvent = {},
        )
    }
}

@Preview
@Composable
private fun PreviewInitialPermission() {
    AppTheme {
        EncryptionMigrationScreenInitial(
            uiState = LegacyEncryptionMigrationUiState.Initial(stage = InitialSubStage.PERMISSION),
            handleUiEvent = {},
        )
    }
}

@Preview
@Composable
private fun PreviewInitialReady() {
    AppTheme {
        EncryptionMigrationScreenInitial(
            uiState = LegacyEncryptionMigrationUiState.Initial(stage = InitialSubStage.READY),
            handleUiEvent = {},
        )
    }
}
