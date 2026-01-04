


package dev.leonlatsch.photok.gallery.ui

import android.net.Uri
import dev.leonlatsch.photok.sort.domain.Sort
import dev.leonlatsch.photok.gallery.components.ImportChoice
import dev.leonlatsch.photok.gallery.components.PhotoTile

sealed interface GalleryUiEvent {
    data class OpenPhoto(val item: PhotoTile) : GalleryUiEvent
    data class OnDelete(val items: List<String>) : GalleryUiEvent
    data class OnExport(val items: List<String>, val target: Uri?) : GalleryUiEvent
    data object OnAddToAlbum : GalleryUiEvent
    data class OnAlbumSelected(val photoIds: List<String>, val albumId: String) : GalleryUiEvent
    data object CancelAlbumSelection : GalleryUiEvent
    data class OnImportChoice(val choice: ImportChoice) : GalleryUiEvent
    data class SortChanged(val sort: Sort) : GalleryUiEvent
}

package dev.leonlatsch.photok.gallery.ui

import android.net.Uri
import dev.leonlatsch.photok.sort.domain.Sort
import dev.leonlatsch.photok.gallery.components.ImportChoice
import dev.leonlatsch.photok.gallery.components.PhotoTile

sealed interface GalleryUiEvent {
    data class OpenPhoto(val item: PhotoTile) : GalleryUiEvent
    data class OnDelete(val items: List<String>) : GalleryUiEvent
    data class OnExport(val items: List<String>, val target: Uri?) : GalleryUiEvent
    data object OnAddToAlbum : GalleryUiEvent
    data class OnAlbumSelected(val photoIds: List<String>, val albumId: String) : GalleryUiEvent
    data object CancelAlbumSelection : GalleryUiEvent
    data class OnImportChoice(val choice: ImportChoice) : GalleryUiEvent
    data class SortChanged(val sort: Sort) : GalleryUiEvent
}