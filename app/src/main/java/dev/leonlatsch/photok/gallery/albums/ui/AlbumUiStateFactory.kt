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

package dev.leonlatsch.photok.gallery.albums.ui

import dev.leonlatsch.photok.gallery.albums.domain.model.Album
import dev.leonlatsch.photok.gallery.albums.toUi
import dev.leonlatsch.photok.gallery.albums.ui.compose.AlbumsUiState
import javax.inject.Inject

class AlbumUiStateFactory @Inject constructor() {
    fun create(albums: List<Album>, showCreateDialog: Boolean): AlbumsUiState {
        if (albums.isEmpty()) {
            return AlbumsUiState.Empty(showCreateDialog)
        }

        return AlbumsUiState.Content(
            albums = albums.map { album -> album.toUi() },
            showCreateDialog = showCreateDialog,
        )
    }
}