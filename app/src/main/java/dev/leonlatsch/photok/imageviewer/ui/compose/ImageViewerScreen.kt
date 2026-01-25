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

import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.imageviewer.ui.ImageViewerItem
import dev.leonlatsch.photok.imageviewer.ui.ImageViewerUiEvent
import dev.leonlatsch.photok.imageviewer.ui.ImageViewerViewModel
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.other.extensions.launchAndIgnoreTimer
import dev.leonlatsch.photok.settings.ui.compose.LocalConfig
import dev.leonlatsch.photok.ui.components.ConfirmationDialog
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun ImageViewerScreen(
    navController: NavController,
    photoUuid: String,
    albumUuid: String,
) {
    CompositionLocalProvider(
        LocalContentColor provides Color.White
    ) {
        val viewModel: ImageViewerViewModel = hiltViewModel()

        val items by viewModel.items.collectAsStateWithLifecycle()
        val handleUiEvent = viewModel::handleUiEvent


        LaunchedEffect(Unit) {
            viewModel.loadItems(albumUuid)
        }


        val context = LocalContext.current

        val player = remember {
            ExoPlayer.Builder(context)
                .setMediaSourceFactory(viewModel.mediaSourceFactory)
                .build()
        }

        DisposableEffect(Unit) {
            onDispose {
                player.release()
            }
        }

        val pagerState = rememberPagerState { items.size }

        val currentItem by remember {
            derivedStateOf {
                items.getOrNull(pagerState.currentPage)
            }
        }

        LaunchedEffect(pagerState.currentPage, items) {
            val item = items.getOrNull(pagerState.currentPage)
            if (item is ImageViewerItem.Video) {
                player.apply {
                    setMediaItem(item.mediaItem)
                    prepare()
                    playWhenReady = true
                }
            } else {
                player.pause()
            }
        }

        LaunchedEffect(items.size) {
            if (items.isNotEmpty()) {
                val initial = items.find { it.photo.uuid == photoUuid }
                initial ?: return@LaunchedEffect

                pagerState.scrollToPage(
                    items.indexOf(initial)
                )
            }
        }

        var showControls by remember { mutableStateOf(true) }

        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
        ) {
            ImageViewerPage(
                item = items[it],
                player = player,
                onClick = {
                    showControls = !showControls
                }
            )
        }

        ImageViewerControls(
            visible = showControls,
            currentItem = currentItem,
            handleUiEvent = handleUiEvent,
            navController = navController,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageViewerControls(
    visible: Boolean,
    currentItem: ImageViewerItem?,
    handleUiEvent: (ImageViewerUiEvent) -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var exportDirectoryUri by remember { mutableStateOf<Uri?>(null) }

    var showDeleteConfirmationDialog by remember {
        mutableStateOf(false)
    }

    var showExportConfirmationDialog by remember {
        mutableStateOf(false)
    }

    val pickExportTargetLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { exportTarget ->
            exportTarget ?: return@rememberLauncherForActivityResult
            exportDirectoryUri = exportTarget
            showExportConfirmationDialog = true
        }

    CompositionLocalProvider(
        LocalContentColor provides Color.White
    ) {
        AnimatedVisibility(
            visible = visible
        ) {
            Box(
                modifier = modifier
                    .fillMaxSize()
            ) {
                // Top

                val statusBarsHeight =
                    WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

                val topGradient = remember {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent,
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(TopAppBarDefaults.TopAppBarExpandedHeight + statusBarsHeight)
                        .background(topGradient)
                )

                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        navigationIconContentColor = LocalContentColor.current,
                        titleContentColor = LocalContentColor.current,
                        actionIconContentColor = LocalContentColor.current,
                        subtitleContentColor = LocalContentColor.current,
                    ),
                    title = {
                        Text(
                            text = currentItem?.photo?.fileName.orEmpty(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
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
                            onClick = {},
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_more),
                                contentDescription = stringResource(R.string.common_more),
                            )
                        }
                    },
                )

                // Bottom

                val activity = LocalActivity.current


                val navBarHeight =
                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

                val bottomGradient = remember {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f),
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(80.dp + navBarHeight) // TODO: More height if video for video controls
                        .background(bottomGradient)
                )

                BottomAppBar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    containerColor = Color.Transparent,
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
//                    BottomActionItem(
//                        text = "Add to album",
//                        icon = R.drawable.ic_add,
//                        action = {},
//                    )
                    BottomActionItem(
                        text = stringResource(R.string.common_delete),
                        icon = R.drawable.ic_delete,
                        action = {
                            showDeleteConfirmationDialog = true
                        },
                    )
                }
            }
        }

        ConfirmationDialog(
            show = showExportConfirmationDialog,
            onDismissRequest = { showExportConfirmationDialog = false },
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
                                context = context,
                            )
                        )
                    }
                }
            }
        )

        ConfirmationDialog(
            show = showDeleteConfirmationDialog,
            onDismissRequest = { showDeleteConfirmationDialog = false },
            text = stringResource(R.string.delete_are_you_sure_this),
            onConfirm = {
                if (currentItem != null) {
                    handleUiEvent(
                        ImageViewerUiEvent.ConfirmDelete(
                            item = currentItem,
                        )
                    )
                }
            }
        )
    }
}

@Composable
fun RowScope.BottomActionItem(
    text: String,
    icon: Int,
    action: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier.weight(1f),
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
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun ControlsPreview() {
    AppTheme() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Blue)
        ) {
            ImageViewerControls(
                visible = true,
                currentItem = ImageViewerItem.Image(
                    photo = Photo(
                        fileName = "Preview File aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                        importedAt = System.currentTimeMillis(),
                        type = PhotoType.JPEG,
                        size = 512L,
                        lastModified = null,
                    )
                ),
                handleUiEvent = {},
                navController = rememberNavController(),
            )
        }
    }
}