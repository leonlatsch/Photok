


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