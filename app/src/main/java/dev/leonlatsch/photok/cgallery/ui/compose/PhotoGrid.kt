/*
 *   Copyright 2020-2023 Leon Latsch
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

package dev.leonlatsch.photok.cgallery.ui.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.cgallery.ui.GalleryUiEvent
import dev.leonlatsch.photok.cgallery.ui.MultiSelectionState
import dev.leonlatsch.photok.cgallery.ui.PhotoTile
import dev.leonlatsch.photok.imageloading.compose.model.EncryptedImageRequestData
import dev.leonlatsch.photok.imageloading.compose.rememberEncryptedImagePainter
import dev.leonlatsch.photok.model.database.entity.PhotoType

@Composable
fun PhotosGrid(
    photos: List<PhotoTile>,
    multiSelectionState: MultiSelectionState,
    handleUiEvent: (GalleryUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    extraTopPadding: Dp = 0.dp
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(top = extraTopPadding)
    ) {
        items(photos, key = { it.uuid }) {
            GalleryPhotoTile(
                photoTile = it,
                multiSelectionActive = multiSelectionState.isActive,
                onClicked = { handleUiEvent(GalleryUiEvent.PhotoClicked(it)) },
                selected = multiSelectionState.selectedItemUUIDs.contains(it.uuid),
                onLongPress = { handleUiEvent(GalleryUiEvent.PhotoLongPressed(it)) }
            )
        }
    }
}

@Preview
@Composable
private fun PhotoGridPreview() {
    PhotosGrid(
        photos = listOf(
            PhotoTile("", PhotoType.JPEG, "1"),
            PhotoTile("", PhotoType.JPEG, "2"),
            PhotoTile("", PhotoType.JPEG, "3"),
            PhotoTile("", PhotoType.JPEG, "4"),
            PhotoTile("", PhotoType.JPEG, "5"),
            PhotoTile("", PhotoType.JPEG, "6"),
        ),
        multiSelectionState = MultiSelectionState(isActive = true, listOf("1", "2", "5")),
        handleUiEvent = {})
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
                    photoTile.internalThumbnailFileName,
                    photoTile.type.mimeType
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