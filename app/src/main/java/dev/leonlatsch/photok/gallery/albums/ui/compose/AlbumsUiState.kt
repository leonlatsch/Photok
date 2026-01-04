


package dev.leonlatsch.photok.gallery.albums.ui.compose

sealed interface AlbumsUiState {
    val showCreateDialog: Boolean

    data class Empty(
        override val showCreateDialog: Boolean = false,
    ) : AlbumsUiState
    data class Content(
        val albums: List<AlbumItem>,
        override val showCreateDialog: Boolean = false,
    ) : AlbumsUiState
}

data class AlbumItem(
    val id: String,
    val name: String,
    val itemCount: Int,
    val albumCover: AlbumCover? = null,
)

data class AlbumCover(
    val filename: String,
    val mimeType: String,
)

package dev.leonlatsch.photok.gallery.albums.ui.compose

sealed interface AlbumsUiState {
    val showCreateDialog: Boolean

    data class Empty(
        override val showCreateDialog: Boolean = false,
    ) : AlbumsUiState
    data class Content(
        val albums: List<AlbumItem>,
        override val showCreateDialog: Boolean = false,
    ) : AlbumsUiState
}

data class AlbumItem(
    val id: String,
    val name: String,
    val itemCount: Int,
    val albumCover: AlbumCover? = null,
)

data class AlbumCover(
    val filename: String,
    val mimeType: String,
)