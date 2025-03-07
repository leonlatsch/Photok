/*
 *   Copyright 2020-2024 Leon Latsch
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

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