package dev.leonlatsch.photok.gallery.components

import dev.leonlatsch.photok.gallery.albums.ui.compose.AlbumItem

data class AlbumPickerUiState(
    val albums: List<AlbumItem> = emptyList(),
)