package dev.leonlatsch.photok.gallery.albums.ui.navigation

import androidx.navigation.NavController
import dev.leonlatsch.photok.gallery.albums.ui.AlbumsFragmentDirections
import javax.inject.Inject

class AlbumsNavigator @Inject constructor() {

    fun navigate(
        event: AlbumsNavigationEvent,
        navController: NavController,
    ) {
        when (event) {
            is AlbumsNavigationEvent.OpenAlbumDetail -> navController.navigate(
                AlbumsFragmentDirections.actionGlobalAlbumDetailFragment(albumUuid = event.uuid)
            )
        }
    }
}

sealed interface AlbumsNavigationEvent {
    data class OpenAlbumDetail(val uuid: String) : AlbumsNavigationEvent
}