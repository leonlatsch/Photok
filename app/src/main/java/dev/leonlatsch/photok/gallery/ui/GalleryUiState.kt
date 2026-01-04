


package dev.leonlatsch.photok.gallery.ui

import android.net.Uri
import dev.leonlatsch.photok.sort.domain.Sort
import dev.leonlatsch.photok.gallery.components.PhotoTile

sealed interface GalleryUiState {

    data object Empty : GalleryUiState

    data class Content(
        val photos: List<PhotoTile> = emptyList(),
        val showAlbumSelectionDialog: Boolean = false,
        val sort: Sort,
    ) : GalleryUiState
}


package dev.leonlatsch.photok.gallery.ui

import android.net.Uri
import dev.leonlatsch.photok.sort.domain.Sort
import dev.leonlatsch.photok.gallery.components.PhotoTile

sealed interface GalleryUiState {

    data object Empty : GalleryUiState

    data class Content(
        val photos: List<PhotoTile> = emptyList(),
        val showAlbumSelectionDialog: Boolean = false,
        val sort: Sort,
    ) : GalleryUiState
}
