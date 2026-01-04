


package dev.leonlatsch.photok.gallery.albums.detail.ui

import dev.leonlatsch.photok.gallery.components.PhotoTile
import dev.leonlatsch.photok.sort.domain.Sort
import dev.leonlatsch.photok.sort.domain.SortConfig

data class AlbumDetailUiState(
    val albumId: String = "",
    val albumName: String = "",
    val photos: List<PhotoTile> = emptyList(),
    val sort: Sort = SortConfig.Album.default,
)

package dev.leonlatsch.photok.gallery.albums.detail.ui

import dev.leonlatsch.photok.gallery.components.PhotoTile
import dev.leonlatsch.photok.sort.domain.Sort
import dev.leonlatsch.photok.sort.domain.SortConfig

data class AlbumDetailUiState(
    val albumId: String = "",
    val albumName: String = "",
    val photos: List<PhotoTile> = emptyList(),
    val sort: Sort = SortConfig.Album.default,
)