


package dev.leonlatsch.photok.gallery.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.leonlatsch.photok.gallery.albums.ui.compose.AlbumItem

@Composable
fun AlbumsGrid(
    albums: List<AlbumItem>,
    onAlbumClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxWidth()
    ) {
        items(albums, key = { it.id }) { album ->
            AlbumTile(
                album = album,
                onAlbumClicked = onAlbumClicked,
                modifier = Modifier.animateItem(),
            )
        }
    }
}


package dev.leonlatsch.photok.gallery.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.leonlatsch.photok.gallery.albums.ui.compose.AlbumItem

@Composable
fun AlbumsGrid(
    albums: List<AlbumItem>,
    onAlbumClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxWidth()
    ) {
        items(albums, key = { it.id }) { album ->
            AlbumTile(
                album = album,
                onAlbumClicked = onAlbumClicked,
                modifier = Modifier.animateItem(),
            )
        }
    }
}
