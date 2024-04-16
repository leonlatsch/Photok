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

package dev.leonlatsch.photok.gallery.albums.detail.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.gallery.albums.detail.ui.AlbumDetailUiEvent
import dev.leonlatsch.photok.gallery.albums.detail.ui.AlbumDetailUiState
import dev.leonlatsch.photok.gallery.ui.components.PhotoGallery
import dev.leonlatsch.photok.gallery.ui.components.PhotoTile
import dev.leonlatsch.photok.gallery.ui.components.rememberMultiSelectionState
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.ui.components.MagicFab
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun AlbumDetailContent(
    uiState: AlbumDetailUiState,
    handleUiEvent: (AlbumDetailUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        val multiSelectionState =
            rememberMultiSelectionState(items = uiState.photos.map { it.uuid })

        PhotoGallery(
            photos = uiState.photos,
            multiSelectionState = multiSelectionState,
            openPhoto = {},
            extraTopPadding = 0.dp
        )

        MagicFab {
            handleUiEvent(AlbumDetailUiEvent.ImportIntoAlbum)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AlbumsDetailScreenPreview() {
    AppTheme {
        AlbumDetailContent(
            uiState = AlbumDetailUiState(
                "Album Name",
                listOf(
                    PhotoTile("file1", PhotoType.JPEG, "uuid1"),
                    PhotoTile("file2", PhotoType.JPEG, "uuid2"),
                    PhotoTile("file3", PhotoType.JPEG, "uuid3"),
                    PhotoTile("file4", PhotoType.JPEG, "uuid4"),
                    PhotoTile("file5", PhotoType.JPEG, "uuid5"),
                    PhotoTile("file6", PhotoType.JPEG, "uuid6"),
                    PhotoTile("file7", PhotoType.JPEG, "uuid7"),
                    PhotoTile("file8", PhotoType.JPEG, "uuid8"),
                )
            ),
            handleUiEvent = {},
        )
    }
}
