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

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.cgallery.ui.GalleryUiState
import dev.leonlatsch.photok.cgallery.ui.PhotoTile
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.entity.PhotoType

@Composable
fun GalleryContent(uiState: GalleryUiState.Content) {
    Column {
        Text(text = "${stringResource(R.string.gallery_all_photos_label)} (${uiState.photos.size})")
        PhotosGrid(uiState.photos, uiState.selectionMode)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
fun GalleryContentPreview() {
    GalleryContent(
        uiState = GalleryUiState.Content(
            selectionMode = true,
            listOf(
                PhotoTile("", PhotoType.JPEG, "1"),
                PhotoTile("", PhotoType.MP4, "2"),
                PhotoTile("", PhotoType.GIF, "3"),
                PhotoTile("", PhotoType.MPEG, "4"),
                PhotoTile("", PhotoType.PNG, "5"),
            )
        )
    )
}

