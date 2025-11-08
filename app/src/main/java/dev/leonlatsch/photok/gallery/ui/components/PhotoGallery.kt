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

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.imageloading.compose.model.EncryptedImageRequestData
import dev.leonlatsch.photok.imageloading.compose.rememberEncryptedImagePainter
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.other.extensions.launchAndIgnoreTimer
import dev.leonlatsch.photok.settings.ui.compose.LocalConfig
import dev.leonlatsch.photok.ui.components.ConfirmationDialog
import dev.leonlatsch.photok.ui.components.MagicFab
import dev.leonlatsch.photok.ui.components.MultiSelectionMenu
import dev.leonlatsch.photok.ui.theme.AppTheme

private const val PORTRAIT_COLUMN_COUNT = 3
private const val LANDSCAPE_COLUMN_COUNT = 6

@Composable
fun PhotoGallery(
    photos: List<PhotoTile>,
    multiSelectionState: MultiSelectionState,
    onOpenPhoto: (PhotoTile) -> Unit,
    onExport: (Uri?) -> Unit,
    onDelete: () -> Unit,
    onImportChoice: (ImportChoice) -> Unit,
    additionalMultiSelectionActions: @Composable (ColumnScope.() -> Unit),
    modifier: Modifier = Modifier,
) {
    val activity = LocalActivity.current
    val importMenuBottomSheetVisible = remember { mutableStateOf(false) }

    // Hide magic fab menu when multi selection active
    LaunchedEffect(multiSelectionState.isActive.value) {
        if (multiSelectionState.isActive.value) {
            importMenuBottomSheetVisible.value = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        PhotoGrid(
            photos = photos,
            multiSelectionState = multiSelectionState,
            openPhoto = onOpenPhoto,
        )

        AnimatedVisibility(
            visible = multiSelectionState.isActive.value.not(),
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomEnd)
        ) {
            MagicFab(
                label = stringResource(R.string.import_menu_fab_label),
                onClick = {
                    importMenuBottomSheetVisible.value = true
                }
            )
        }

        ImportMenuBottomSheet(
            openState = importMenuBottomSheetVisible,
            onImportChoice = onImportChoice,
        )

        var showDeleteConfirmationDialog by remember {
            mutableStateOf(false)
        }

        var showExportConfirmationDialog by remember {
            mutableStateOf(false)
        }

        var exportDirectoryUri by remember { mutableStateOf<Uri?>(null) }

        val pickExportTargetLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { exportTarget ->
                exportTarget ?: return@rememberLauncherForActivityResult
                exportDirectoryUri = exportTarget
                showExportConfirmationDialog = true
            }

        ConfirmationDialog(
            show = showDeleteConfirmationDialog,
            onDismissRequest = { showDeleteConfirmationDialog = false },
            text = stringResource(
                R.string.delete_are_you_sure,
                multiSelectionState.selectedItems.value.size
            ),
            onConfirm = {
                onDelete()
                multiSelectionState.cancelSelection()
            }
        )

        ConfirmationDialog(
            show = showExportConfirmationDialog,
            onDismissRequest = { showExportConfirmationDialog = false },
            text = stringResource(
                if (LocalConfig.current?.deleteExportedFiles == true) {
                    R.string.export_and_delete_are_you_sure
                } else {
                    R.string.export_are_you_sure
                },
                multiSelectionState.selectedItems.value.size
            ),
            onConfirm = {
                onExport(exportDirectoryUri)
                multiSelectionState.cancelSelection()
            }
        )

        MultiSelectionMenu(
            modifier = Modifier.align(Alignment.BottomCenter),
            multiSelectionState = multiSelectionState,
        ) {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_select_all),
                        contentDescription = null
                    )
                },
                text = { Text(stringResource(R.string.menu_ms_select_all)) },
                onClick = {
                    multiSelectionState.selectAll()
                    multiSelectionState.dismissMore()
                },
            )
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = null
                    )
                },
                text = { Text(stringResource(R.string.common_delete)) },
                onClick = {
                    showDeleteConfirmationDialog = true
                    multiSelectionState.dismissMore()
                },
            )
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_export),
                        contentDescription = null
                    )
                },
                text = { Text(stringResource(R.string.common_export)) },
                onClick = {
                    pickExportTargetLauncher.launchAndIgnoreTimer(
                        input = null,
                        activity = activity,
                    )
                    multiSelectionState.dismissMore()
                },
            )

            additionalMultiSelectionActions()
        }
    }
}

@Composable
fun PhotoGrid(
    photos: List<PhotoTile>,
    multiSelectionState: MultiSelectionState,
    openPhoto: (PhotoTile) -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
) {
    val columnCount = when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> PORTRAIT_COLUMN_COUNT
        Configuration.ORIENTATION_LANDSCAPE -> LANDSCAPE_COLUMN_COUNT
        else -> PORTRAIT_COLUMN_COUNT
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columnCount),
        modifier = modifier.fillMaxWidth(),
        state = gridState
    ) {
        items(photos, key = { it.uuid }) {
            GalleryPhotoTile(
                photoTile = it,
                multiSelectionActive = multiSelectionState.isActive.value,
                onClicked = {
                    if (multiSelectionState.isActive.value.not()) {
                        openPhoto(it)
                        return@GalleryPhotoTile
                    }

                    if (multiSelectionState.selectedItems.value.contains(it.uuid)) {
                        multiSelectionState.deselectItem(it.uuid)
                    } else {
                        multiSelectionState.selectItem(it.uuid)
                    }
                },
                selected = multiSelectionState.selectedItems.value.contains(it.uuid),
                onLongPress = {
                    if (multiSelectionState.isActive.value.not()) {
                        multiSelectionState.selectItem(it.uuid)
                    }
                }
            )
        }
    }
}

private val VideoIconSize = 20.dp
private val SelectedPadding = 15.dp
private val CheckmarkPadding = SelectedPadding - 9.dp

@Composable
fun Modifier.multiSelectionItem(selected: Boolean): Modifier {
    val animatedPadding by animateDpAsState(
        targetValue = if (selected) { SelectedPadding } else { 0.dp }
    )
    val animatedShape by animateDpAsState(
        targetValue = if (selected) { 12.dp } else { 0.dp }
    )

    return this
        .padding(animatedPadding)
        .clip(RoundedCornerShape(animatedShape))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GalleryPhotoTile(
    photoTile: PhotoTile,
    multiSelectionActive: Boolean,
    selected: Boolean,
    onClicked: () -> Unit,
    onLongPress: () -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(.5.dp)
            .combinedClickable(
                role = Role.Image,
                onClick = onClicked,
                onLongClick = onLongPress,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            )
    ) {
        val contentModifier = Modifier
            .multiSelectionItem(selected)
            .fillMaxSize()
            .aspectRatio(1f)

        if (LocalInspectionMode.current) {
            Box(
                modifier = contentModifier.background(Color.DarkGray)
            )
        } else {
            val requestData = remember(photoTile) {
                EncryptedImageRequestData(
                    internalFileName = photoTile.internalThumbnailFileName,
                    mimeType = photoTile.type.mimeType
                )
            }

            Image(
                painter = rememberEncryptedImagePainter(requestData),
                contentDescription = photoTile.fileName,
                modifier = contentModifier
            )
        }

        AnimatedVisibility(
            visible = photoTile.type.isVideo && !selected,
            enter = scaleIn(),
            exit = scaleOut(),
            modifier = Modifier
                .padding(2.dp)
                .size(VideoIconSize)
                .align(Alignment.BottomStart)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_videocam),
                contentDescription = null,
                tint = Color.LightGray,
            )
        }

        AnimatedVisibility(
            visible = multiSelectionActive && selected,
            enter = scaleIn(),
            exit = scaleOut(),
            ) {
            Icon(
                painter = painterResource(R.drawable.ic_check_circle),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .padding(CheckmarkPadding)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background)
                    .align(Alignment.TopStart)
            )
        }
    }
}

@Preview
@Composable
private fun PhotoGridPreview() {
    AppTheme {
        Scaffold {
            PhotoGallery(
                modifier = Modifier.padding(it),
                photos = listOf(
                    PhotoTile("", PhotoType.JPEG, "1"),
                    PhotoTile("", PhotoType.MP4, "2"),
                    PhotoTile("", PhotoType.MP4, "3"),
                    PhotoTile("", PhotoType.JPEG, "4"),
                    PhotoTile("", PhotoType.JPEG, "5"),
                    PhotoTile("", PhotoType.MP4, "6"),
                ),
                multiSelectionState = MultiSelectionState(
                    allItems = listOf("1", "2", "3"),
                ),
                onOpenPhoto = {},
                onDelete = {},
                onExport = {},
                onImportChoice = {},
                additionalMultiSelectionActions = {},
            )
        }
    }
}

@Preview
@Composable
private fun PhotoGridPreviewWithSelection() {
    AppTheme {
        Scaffold {
            PhotoGallery(
                modifier = Modifier.padding(it),
                photos = listOf(
                    PhotoTile("", PhotoType.JPEG, "1"),
                    PhotoTile("", PhotoType.MP4, "2"),
                    PhotoTile("", PhotoType.MP4, "3"),
                    PhotoTile("", PhotoType.JPEG, "4"),
                    PhotoTile("", PhotoType.JPEG, "5"),
                    PhotoTile("", PhotoType.MP4, "6"),
                ),
                multiSelectionState = MultiSelectionState(
                    allItems = listOf("1", "2", "3"),
                ).apply {
                    selectItem("2")
                    selectItem("3")
                },
                onOpenPhoto = {},
                onDelete = {},
                onExport = {},
                onImportChoice = {},
                additionalMultiSelectionActions = {},
            )
        }
    }
}