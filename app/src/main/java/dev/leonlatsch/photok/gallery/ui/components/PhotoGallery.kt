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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.imageloading.compose.model.EncryptedImageRequestData
import dev.leonlatsch.photok.imageloading.compose.rememberEncryptedImagePainter
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.settings.ui.compose.LocalConfig
import dev.leonlatsch.photok.ui.components.ConfirmationDialog
import dev.leonlatsch.photok.ui.components.MagicFab
import dev.leonlatsch.photok.ui.components.MultiSelectionMenu

private const val PORTRAIT_COLUMN_COUNT = 3
private const val LANDSCAPE_COLUMN_COUNT = 6

@Composable
fun PhotoGallery(
    photos: List<PhotoTile>,
    multiSelectionState: MultiSelectionState,
    onOpenPhoto: (PhotoTile) -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
    onMagicFabClicked: () -> Unit,
    additionalMultiSelectionActions: @Composable (ColumnScope.(closeActions: () -> Unit) -> Unit),
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        PhotoGrid(
            photos = photos,
            multiSelectionState = multiSelectionState,
            openPhoto = onOpenPhoto,
        )

        AnimatedVisibility(
            visible = multiSelectionState.isActive.value.not(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
        ) {
            MagicFab {
                onMagicFabClicked()
            }
        }

        var showDeleteConfirmationDialog by remember {
            mutableStateOf(false)
        }

        var showExportConfirmationDialog by remember {
            mutableStateOf(false)
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
                if (LocalConfig.current.deleteExportedFiles) {
                    R.string.export_and_delete_are_you_sure
                } else {
                    R.string.export_are_you_sure
                },
                multiSelectionState.selectedItems.value.size
            ),
            onConfirm = {
                onExport()
                multiSelectionState.cancelSelection()
            }
        )

        AnimatedVisibility(
            visible = multiSelectionState.isActive.value,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(vertical = 24.dp, horizontal = 12.dp)
        ) {
            MultiSelectionMenu(
                multiSelectionState = multiSelectionState,
            ) { closeActions ->
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
                        closeActions()
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
                        closeActions()
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
                        showExportConfirmationDialog = true
                        closeActions()
                    },
                )

                additionalMultiSelectionActions(
                    closeActions = closeActions,
                )
            }
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
                onClick = onClicked,
                onLongClick = onLongPress
            )
    ) {
        val contentModifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)

        if (LocalInspectionMode.current) {
            Box(
                modifier = contentModifier.background(Color.Red)
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

        if (photoTile.type.isVideo) {
            Icon(
                painter = painterResource(R.drawable.ic_videocam),
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier
                    .padding(2.dp)
                    .size(VideoIconSize)
                    .align(Alignment.BottomStart)
            )
        }

        if (multiSelectionActive) {

            if (selected) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(0.4f)
                        .background(Color.Black)
                )
            }

            Checkbox(
                checked = selected,
                onCheckedChange = { onClicked() },
                modifier = Modifier.align(Alignment.BottomEnd),
                colors = CheckboxDefaults.colors().copy(
                    checkedBoxColor = colorResource(R.color.colorPrimary),
                    checkedBorderColor = colorResource(R.color.colorPrimaryDark)
                )
            )
        }
    }
}

@Preview
@Composable
private fun PhotoGridPreview() {
    PhotoGallery(
        photos = listOf(
            PhotoTile("", PhotoType.JPEG, "1"),
            PhotoTile("", PhotoType.JPEG, "2"),
            PhotoTile("", PhotoType.JPEG, "3"),
            PhotoTile("", PhotoType.JPEG, "4"),
            PhotoTile("", PhotoType.JPEG, "5"),
            PhotoTile("", PhotoType.JPEG, "6"),
        ),
        multiSelectionState = MultiSelectionState(listOf("2", "3", "5")),
        onOpenPhoto = {},
        onDelete = {},
        onExport = {},
        onMagicFabClicked = {},
        additionalMultiSelectionActions = {},
    )
}