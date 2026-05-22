/*
 *   Copyright 2020-2026 Leon Latsch
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

package dev.leonlatsch.photok.imageviewer.ui.compose

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.components.AlbumPickerDialog
import dev.leonlatsch.photok.imageviewer.ui.ImageViewerItem
import dev.leonlatsch.photok.imageviewer.ui.ImageViewerUiEvent
import dev.leonlatsch.photok.imageviewer.ui.ImageViewerUiState
import dev.leonlatsch.photok.other.extensions.launchAndIgnoreTimer
import dev.leonlatsch.photok.settings.ui.compose.LocalConfig
import dev.leonlatsch.photok.ui.components.ConfirmationDialog
import dev.leonlatsch.photok.ui.components.RoundedDropdownMenu
import dev.leonlatsch.photok.uicomponnets.Dialogs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ImageViewerControls(
    showControls: Boolean,
    currentItem: ImageViewerItem?,
    uiState: ImageViewerUiState,
    handleUiEvent: (ImageViewerUiEvent) -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    var exportDirectoryUri by remember { mutableStateOf<Uri?>(null) }
    val isPinned = currentItem?.photo?.uuid in uiState.pinnedPhotoIds
    val showPinnedIndicator = uiState.albumUuid != null && isPinned

    val pickExportTargetLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { exportTarget ->
            exportTarget ?: return@rememberLauncherForActivityResult
            exportDirectoryUri = exportTarget

            handleUiEvent(ImageViewerUiEvent.UpdateCurrentDialog(ImageViewerUiState.Dialog.ConfirmExport))
        }

    CompositionLocalProvider(
        LocalContentColor provides Color.White
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
        ) {

            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        navigationIconContentColor = LocalContentColor.current,
                        titleContentColor = LocalContentColor.current,
                        actionIconContentColor = LocalContentColor.current,
                        subtitleContentColor = LocalContentColor.current,
                    ),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AnimatedVisibility(showPinnedIndicator) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_pin),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(18.dp),
                                )
                            }

                            AnimatedContent(currentItem?.photo?.fileName.orEmpty()) {
                                Text(
                                    text = it,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.navigateUp() }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_back),
                                contentDescription = stringResource(R.string.common_cancel)
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                handleUiEvent(
                                    ImageViewerUiEvent.UpdateCurrentDialog(
                                        ImageViewerUiState.Dialog.MoreMenu,
                                    )
                                )
                            },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_more),
                                contentDescription = stringResource(R.string.common_more),
                            )
                        }

                        // Placed in actions for alignment
                        MoreMenu(
                            visible = uiState.inputs.currentDialog == ImageViewerUiState.Dialog.MoreMenu,
                            onDismissRequest = {
                                handleUiEvent(ImageViewerUiEvent.UpdateCurrentDialog(null))
                            },
                            currentItem = currentItem,
                            uiState = uiState,
                            handleUiEvent = handleUiEvent,
                        )
                    },
                )
            }

            // Bottom

            val activity = LocalActivity.current

            val configuration = LocalConfiguration.current

            val showButtons by remember(configuration.orientation, showControls, currentItem) {
                derivedStateOf {
                    when (currentItem) {
                        is ImageViewerItem.Image -> showControls
                        is ImageViewerItem.Video -> showControls && configuration.orientation != ORIENTATION_LANDSCAPE
                        null -> showControls
                    }
                }
            }

            AnimatedVisibility(
                visible = showButtons,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                BottomAppBar(
                    containerColor = Color.Transparent,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        BottomActionItem(
                            text = stringResource(R.string.common_export),
                            icon = R.drawable.ic_export,
                            action = {
                                pickExportTargetLauncher.launchAndIgnoreTimer(
                                    input = null,
                                    activity = activity,
                                )
                            },
                        )
                        BottomActionItem(
                            text = stringResource(R.string.menu_ms_add_to_album),
                            icon = R.drawable.ic_add,
                            action = {
                                handleUiEvent(
                                    ImageViewerUiEvent.UpdateCurrentDialog(
                                        ImageViewerUiState.Dialog.AlbumPicker
                                    )
                                )
                            },
                        )
                        BottomActionItem(
                            text = stringResource(R.string.common_delete),
                            icon = R.drawable.ic_delete,
                            action = {
                                handleUiEvent(
                                    ImageViewerUiEvent.UpdateCurrentDialog(
                                        ImageViewerUiState.Dialog.ConfirmDelete
                                    )
                                )
                            },
                        )
                    }
                }
            }
        }

        ConfirmationDialog(
            show = uiState.inputs.currentDialog == ImageViewerUiState.Dialog.ConfirmExport,
            onDismissRequest = {
                handleUiEvent(ImageViewerUiEvent.UpdateCurrentDialog(null))
            },
            text = stringResource(
                if (LocalConfig.current?.deleteExportedFiles == true) {
                    R.string.export_and_delete_are_you_sure_this
                } else {
                    R.string.export_are_you_sure_this
                },
            ),
            onConfirm = {
                if (currentItem != null) {
                    exportDirectoryUri?.let {
                        handleUiEvent(
                            ImageViewerUiEvent.ConfirmExport(
                                item = currentItem,
                                target = it,
                            )
                        )
                    }
                }
            }
        )

        ConfirmationDialog(
            show = uiState.inputs.currentDialog == ImageViewerUiState.Dialog.ConfirmDelete,
            onDismissRequest = {
                handleUiEvent(ImageViewerUiEvent.UpdateCurrentDialog(null))
            },
            text = stringResource(R.string.delete_are_you_sure_this),
            onConfirm = {
                if (currentItem != null) {
                    val itemCount = uiState.items.size
                    handleUiEvent(
                        ImageViewerUiEvent.ConfirmDelete(
                            item = currentItem,
                        )
                    )

                    if (itemCount <= 1) {
                        navController.navigateUp()
                    }
                }
            }
        )

        AlbumPickerDialog(
            visible = uiState.inputs.currentDialog == ImageViewerUiState.Dialog.AlbumPicker,
            selectedItemIds = if (currentItem == null) emptyList() else listOf(currentItem.photo.uuid),
            onDismissRequest = {
                handleUiEvent(ImageViewerUiEvent.UpdateCurrentDialog(null))
            },
        )
    }
}

private val PlaybackSpeedOptions = setOf(
    0.25f,
    0.5f,
    1f,
    1.5f,
    2f,
)

@Composable
private fun MoreMenu(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    currentItem: ImageViewerItem?,
    uiState: ImageViewerUiState,
    handleUiEvent: (ImageViewerUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    RoundedDropdownMenu(
        expanded = visible,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        val isAlbumScoped = uiState.albumUuid != null
        val isPinned = currentItem?.photo?.uuid in uiState.pinnedPhotoIds
        val pinSuccessMessage = stringResource(R.string.viewer_pin_confirmation)
        val unpinSuccessMessage = stringResource(R.string.viewer_unpin_confirmation)

        if (isAlbumScoped && currentItem != null) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(
                            if (isPinned) {
                                R.string.menu_ms_unpin
                            } else {
                                R.string.menu_ms_pin
                            }
                        ),
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(
                            if (isPinned) {
                                R.drawable.ic_pin_off
                            } else {
                                R.drawable.ic_pin
                            }
                        ),
                        contentDescription = null,
                    )
                },
                onClick = {
                    handleUiEvent(
                        ImageViewerUiEvent.SetPinned(
                            photoUuid = currentItem.photo.uuid,
                            pinned = !isPinned,
                        )
                    )
                    Dialogs.showShortToast(
                        context,
                        if (isPinned) unpinSuccessMessage else pinSuccessMessage
                    )

                    onDismissRequest()
                }
            )

            HorizontalDivider()
        }

        if (currentItem is ImageViewerItem.Video) {


            val loopingEnabledText = stringResource(R.string.view_photo_loop_video_enabled)
            val loopingDisabledText = stringResource(R.string.view_photo_loop_video_disabled)

            DropdownMenuItem(
                text = {
                    val text = remember(uiState.loopVideos) {
                        if (uiState.loopVideos) {
                            loopingEnabledText
                        } else {
                            loopingDisabledText
                        }
                    }

                    Text(
                        text = text,
                    )
                },
                leadingIcon = {
                    val icon = remember(uiState.loopVideos) {
                        if (uiState.loopVideos) {
                            R.drawable.media3_icon_repeat_all // Icon for all but we just do repeat one
                        } else {
                            R.drawable.ic_repeat_off
                        }
                    }

                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                    )
                },
                onClick = {
                    val newValue = !uiState.loopVideos

                    val message = if (newValue) {
                        loopingEnabledText
                    } else {
                        loopingDisabledText
                    }

                    Dialogs.showShortToast(context, message)
                    handleUiEvent(ImageViewerUiEvent.UpdateLoopVideos(newValue))
                    onDismissRequest()
                }
            )

            var showPlaybackSpeed by remember { mutableStateOf(false) }

            val playbackSpeedText = stringResource(R.string.view_photo_video_playback_speed)

            DropdownMenuItem(
                text = {
                    Text(
                        text = playbackSpeedText,
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_slow_motion),
                        contentDescription = null,
                    )
                },
                onClick = { showPlaybackSpeed = true }
            )

            RoundedDropdownMenu(
                expanded = showPlaybackSpeed,
                onDismissRequest = { showPlaybackSpeed = false },
            ) {
                for (option in PlaybackSpeedOptions) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "${option}x",
                            )
                        },
                        trailingIcon = {
                            AnimatedVisibility(
                                visible = option == uiState.videoPlaybackSpeed,
                                enter = fadeIn(),
                                exit = fadeOut(),
                            ) {
                                Icon(
                                    modifier = Modifier.size(18.dp),
                                    painter = painterResource(R.drawable.ic_check),
                                    contentDescription = null,
                                )
                            }
                        },
                        onClick = {
                            handleUiEvent(ImageViewerUiEvent.UpdateVideoPlaybackSpeed(option))
                            Dialogs.showShortToast(context, "$playbackSpeedText ${option}x")
                            showPlaybackSpeed = false
                        }
                    )
                }
            }

            HorizontalDivider()

        }

        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(R.string.view_photo_detail_title),
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_info),
                    contentDescription = null,
                )
            },
            onClick = {
                handleUiEvent(ImageViewerUiEvent.UpdateCurrentDialog(ImageViewerUiState.Dialog.DetailsSheet))
            }
        )
    }

    currentItem?.let {
        ImageDetailsSheet(
            visible = uiState.inputs.currentDialog == ImageViewerUiState.Dialog.DetailsSheet,
            onDismissRequest = {
                handleUiEvent(ImageViewerUiEvent.UpdateCurrentDialog(null))
            },
            photo = it.photo
        )
    }
}

@Composable
fun BottomActionItem(
    text: String,
    icon: Int,
    action: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = action)
                .padding(10.dp)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
            )
            Text(
                text = text,
                maxLines = 1,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
