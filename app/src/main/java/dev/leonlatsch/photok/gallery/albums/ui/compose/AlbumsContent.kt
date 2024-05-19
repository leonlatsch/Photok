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

package dev.leonlatsch.photok.gallery.albums.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.leonlatsch.photok.gallery.albums.ui.AlbumsUiEvent
import dev.leonlatsch.photok.gallery.ui.components.AlbumsGrid
import dev.leonlatsch.photok.ui.components.MagicFab
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun AlbumsContent(
    content: AlbumsUiState.Content,
    handleUiEvent: (AlbumsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        AlbumsGrid(
            albums = content.albums,
            onAlbumClicked = { handleUiEvent(AlbumsUiEvent.OpenAlbum(it)) },
            modifier = Modifier.fillMaxWidth(),
        )

        MagicFab {
            handleUiEvent(AlbumsUiEvent.ShowCreateDialog)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AlbumsContentPreview() {
    AppTheme {
        AlbumsContent(
            content = AlbumsUiState.Content(
                listOf(
                    AlbumItem(
                        id = "1",
                        name = "Album 1",
                        itemCount = 10,
                    ),
                    AlbumItem(
                        id = "2",
                        name = "Album 2",
                        itemCount = 20,
                    ),
                    AlbumItem(
                        id = "3",
                        name = "Album 3",
                        itemCount = 30,
                    ),
                    AlbumItem(
                        id = "4",
                        name = "Album 4",
                        itemCount = 40
                    ),
                    AlbumItem(
                        id = "5",
                        name = "Album 5",
                        itemCount = 50
                    ),
                )
            ),
            handleUiEvent = {}
        )
    }
}