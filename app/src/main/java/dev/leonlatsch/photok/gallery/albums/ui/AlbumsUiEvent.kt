package dev.leonlatsch.photok.gallery.albums.ui

sealed interface AlbumsUiEvent {
    data object ShowCreateDialog : AlbumsUiEvent
    data object HideCreateDialog : AlbumsUiEvent
    data class OpenAlbum(val uuid: String) : AlbumsUiEvent
}