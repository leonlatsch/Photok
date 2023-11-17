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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.cgallery.ui.GalleryUiEvent
import dev.leonlatsch.photok.cgallery.ui.GalleryUiState
import dev.leonlatsch.photok.cgallery.ui.MultiSelectionState
import dev.leonlatsch.photok.cgallery.ui.PhotoTile
import dev.leonlatsch.photok.model.database.entity.PhotoType
import java.util.UUID

@Composable
fun GalleryContent(uiState: GalleryUiState.Content, handleUiEvent: (GalleryUiEvent) -> Unit) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.scrollable(scrollState, orientation = Orientation.Vertical)
    ) {
        PhotosGrid(
            photos = uiState.photos,
            multiSelectionState = uiState.multiSelectionState,
            handleUiEvent = handleUiEvent,
            modifier = Modifier.fillMaxHeight()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(colorResource(R.color.background), Color.Transparent)
                    )
                )
        )

        Text(
            text = stringResource(R.string.app_name),
            fontFamily = FontFamily(Font(R.font.lobster_regular)),
            color = colorResource(R.color.appTitleColor),
            fontSize = 40.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
        )

        AnimatedVisibility(
            visible = uiState.multiSelectionState.isActive.not(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
        ) {
            FloatingActionButton(onClick = { handleUiEvent(GalleryUiEvent.OpenImportMenu) }) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

        AnimatedVisibility(
            visible = uiState.multiSelectionState.isActive,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
        ) {
            GalleryInteractionsRow(
                onClose = { handleUiEvent(GalleryUiEvent.CancelMultiSelect) },
                onSelectAll = { handleUiEvent(GalleryUiEvent.SelectAll) },
                onDelete = { handleUiEvent(GalleryUiEvent.OnDelete) },
                onExport = { handleUiEvent(GalleryUiEvent.OnExport) },
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
fun GalleryContentPreview() {
    GalleryContent(
        uiState = GalleryUiState.Content(
            selectionMode = true,
            listOf(
                PhotoTile("", PhotoType.JPEG, UUID.randomUUID().toString()),
                PhotoTile("", PhotoType.MP4, UUID.randomUUID().toString()),
                PhotoTile("", PhotoType.GIF, UUID.randomUUID().toString()),
                PhotoTile("", PhotoType.MPEG, UUID.randomUUID().toString()),
                PhotoTile("", PhotoType.PNG, UUID.randomUUID().toString()),
                PhotoTile("", PhotoType.PNG, UUID.randomUUID().toString()),
                PhotoTile("", PhotoType.PNG, UUID.randomUUID().toString()),
                PhotoTile("", PhotoType.PNG, UUID.randomUUID().toString()),
                PhotoTile("", PhotoType.PNG, UUID.randomUUID().toString()),
                PhotoTile("", PhotoType.PNG, UUID.randomUUID().toString()),
                PhotoTile("", PhotoType.PNG, UUID.randomUUID().toString()),
                PhotoTile("", PhotoType.PNG, UUID.randomUUID().toString()),
                PhotoTile("", PhotoType.PNG, UUID.randomUUID().toString()),
                PhotoTile("", PhotoType.PNG, UUID.randomUUID().toString()),
                PhotoTile("", PhotoType.PNG, UUID.randomUUID().toString()),
            ),
            multiSelectionState = MultiSelectionState(
                isActive = true,
                selectedItemUUIDs = listOf()
            )
        ),
        handleUiEvent = {},
    )
}

