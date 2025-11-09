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

package dev.leonlatsch.photok.gallery.albums.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.gallery.albums.domain.model.Album
import dev.leonlatsch.photok.gallery.albums.ui.compose.AlbumsUiState
import dev.leonlatsch.photok.gallery.albums.ui.navigation.AlbumsNavigationEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val albumsRepositoryImpl: AlbumRepository,
    private val albumUiStateFactory: AlbumUiStateFactory,
) : ViewModel() {

    private val showCreateDialog = MutableStateFlow(false)


    val uiState: StateFlow<AlbumsUiState> = combine(
        albumsRepositoryImpl.observeAllAlbumsWithPhotos(),
        showCreateDialog
    ) { albums, showCreateDialog ->
        albumUiStateFactory.create(albums, showCreateDialog)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), AlbumsUiState.Empty())

    private val navEventChannel = Channel<AlbumsNavigationEvent>()
    val navEvent = navEventChannel.receiveAsFlow()

    fun handleUiEvent(event: AlbumsUiEvent) {
        when (event) {
            is AlbumsUiEvent.CreateAlbum -> {
                viewModelScope.launch {
                    albumsRepositoryImpl.createAlbum(
                        Album(
                            name = event.name,
                            files = emptyList()
                        )
                    )
                }
            }

            AlbumsUiEvent.ShowCreateDialog -> showCreateDialog.value = true
            AlbumsUiEvent.HideCreateDialog -> showCreateDialog.value = false
            is AlbumsUiEvent.OpenAlbum -> navEventChannel.trySend(
                AlbumsNavigationEvent.OpenAlbumDetail(
                    event.uuid
                )
            )
        }
    }
}

