package dev.leonlatsch.photok.gallery.albums.detail.ui

import android.net.Uri
import dev.leonlatsch.photok.sort.domain.Sort
import dev.leonlatsch.photok.gallery.components.ImportChoice
import dev.leonlatsch.photok.gallery.components.PhotoTile

sealed interface AlbumDetailUiEvent {
    data class OpenPhoto(val item: PhotoTile) : AlbumDetailUiEvent
    data class OnDelete(val items: List<String>) : AlbumDetailUiEvent
    data class OnExport(val items: List<String>, val target: Uri?) : AlbumDetailUiEvent
    data class RemoveFromAlbum(val items: List<String>) : AlbumDetailUiEvent
    data object DeleteAlbum : AlbumDetailUiEvent
    data class RenameAlbum(val newName: String) : AlbumDetailUiEvent
    data class OnImportChoice(val choice: ImportChoice) : AlbumDetailUiEvent
    data class SortChanged(val sort: Sort) : AlbumDetailUiEvent
}