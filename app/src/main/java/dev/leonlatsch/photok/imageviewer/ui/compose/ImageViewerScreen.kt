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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.components.AlbumPickerDialog
import dev.leonlatsch.photok.imageviewer.ui.ImageViewerItem
import dev.leonlatsch.photok.imageviewer.ui.ImageViewerSystemBarsController
import dev.leonlatsch.photok.imageviewer.ui.ImageViewerUiEvent
import dev.leonlatsch.photok.imageviewer.ui.ImageViewerViewModel
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.other.extensions.launchAndIgnoreTimer
import dev.leonlatsch.photok.settings.ui.compose.LocalConfig
import dev.leonlatsch.photok.ui.components.ConfirmationDialog
import dev.leonlatsch.photok.ui.components.RoundedDropdownMenu
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun ImageViewerScreen(
    navController: NavController,
    photoUuid: String,
    albumUuid: String?,
) {
    CompositionLocalProvider(
        LocalContentColor provides Color.White
    ) {
        val viewModel: ImageViewerViewModel =
            hiltViewModel<ImageViewerViewModel, ImageViewerViewModel.Factory>(
                creationCallback = { factory ->
                    factory.create(albumUuid)
                }
            )

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val handleUiEvent = viewModel::handleUiEvent

        val context = LocalContext.current
        val activity = LocalActivity.current

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

        val pagerState = rememberPagerState { uiState.items.size }

        val currentItem by remember {
            derivedStateOf {
                uiState.items.getOrNull(pagerState.currentPage)
            }
        }

        LaunchedEffect(pagerState.settledPage, uiState.items) {
            val item = uiState.items.getOrNull(pagerState.settledPage)
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

        LaunchedEffect(uiState.items.size) {
            if (uiState.items.isNotEmpty()) {
                val initial = uiState.items.find { it.photo.uuid == photoUuid }
                initial ?: return@LaunchedEffect

                pagerState.scrollToPage(
                    uiState.items.indexOf(initial)
                )
            }
        }

        var showControls by remember { mutableStateOf(false) }

        ImageViewerSystemBarsController(visible = showControls)

        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
        ) { pageIndex ->
            ImageViewerPage(
                item = uiState.items[pageIndex],
                player = player,
                updateShowControls = { newValue -> showControls = newValue },
                showControls = showControls,
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

private val GradientExtraSpace = 40.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    var showDeleteConfirmationDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showExportConfirmationDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showAlbumPickerDialog by rememberSaveable {
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

                val statusBarsHeight = WindowInsets
                    .statusBarsIgnoringVisibility
                    .asPaddingValues()
                    .calculateTopPadding()

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
                        .height(TopAppBarDefaults.TopAppBarExpandedHeight + statusBarsHeight + GradientExtraSpace)
                        .background(topGradient)
                )

                var showMoreMenu by remember { mutableStateOf(false) }

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
                            onClick = { showMoreMenu = true },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_more),
                                contentDescription = stringResource(R.string.common_more),
                            )
                        }

                        // Placed in actions for alignment
                        MoreMenu(
                            visible = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                            currentItem = currentItem,
                        )
                    },
                )

                // Bottom

                val activity = LocalActivity.current


                val navBarHeight = WindowInsets
                    .navigationBarsIgnoringVisibility
                    .asPaddingValues()
                    .calculateBottomPadding()

                val bottomGradient = remember {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f),
                        )
                    )
                }

                // Extra large gradient for videos because of controls
                val videoExtraSpace = if (currentItem is ImageViewerItem.Video) {
                    50.dp
                } else {
                    0.dp
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(80.dp + navBarHeight + GradientExtraSpace + videoExtraSpace)
                        .background(bottomGradient)
                )

                BottomAppBar(
                    modifier = Modifier.align(Alignment.BottomCenter),
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
                            action = { showAlbumPickerDialog = true },
                        )
                        BottomActionItem(
                            text = stringResource(R.string.common_delete),
                            icon = R.drawable.ic_delete,
                            action = { showDeleteConfirmationDialog = true },
                        )
                    }
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

        AlbumPickerDialog(
            visible = showAlbumPickerDialog,
            selectedItemIds = if (currentItem == null) emptyList() else listOf(currentItem.photo.uuid),
            onDismissRequest = { showAlbumPickerDialog = false },
        )
    }
}

@Composable
private fun MoreMenu(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    currentItem: ImageViewerItem?,
    modifier: Modifier = Modifier,
) {
    var showDetailsSheet by remember { mutableStateOf(false) }

    RoundedDropdownMenu(
        expanded = visible,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
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
                onDismissRequest()
                showDetailsSheet = true
            }
        )
    }

    currentItem?.let {
        ImageDetailsSheet(
            visible = showDetailsSheet,
            onDismissRequest = { showDetailsSheet = false },
            photo = it.photo
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