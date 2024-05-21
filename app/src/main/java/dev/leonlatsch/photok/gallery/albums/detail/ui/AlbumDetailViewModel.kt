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

package dev.leonlatsch.photok.gallery.albums.detail.ui

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.gallery.albums.domain.model.Album
import dev.leonlatsch.photok.gallery.ui.components.PhotoTile
import dev.leonlatsch.photok.gallery.ui.navigation.PhotoAction
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

const val ALBUM_DETAIL_UUID = "album_uuid"

@HiltViewModel(assistedFactory = AlbumDetailViewModel.Factory::class)
class AlbumDetailViewModel @AssistedInject constructor(
    @Assisted(ALBUM_DETAIL_UUID) private val albumUUID: String,
    private val albumsRepository: AlbumRepository,
    private val resources: Resources,
) : ViewModel() {

    private val albumFlow = albumsRepository.observeAlbumWithPhotos(albumUUID)
        .stateIn(viewModelScope, SharingStarted.Lazily, Album("", "", emptyList()))

    private val photoActionsChannel = Channel<PhotoAction>()
    val photoActions = photoActionsChannel.receiveAsFlow()

    val uiState = albumFlow.map { album ->
        AlbumDetailUiState(
            albumName = album.name,
            photos = album.files.map {
                PhotoTile(
                    it.internalThumbnailFileName,
                    it.type,
                    it.uuid
                )
            }
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, AlbumDetailUiState())

    private val navEventsChannel = Channel<AlbumDetailNavigator.NavigationEvent>()
    val navEvents = navEventsChannel.receiveAsFlow()

    fun handleUiEvent(event: AlbumDetailUiEvent) {
        when (event) {
            is AlbumDetailUiEvent.OnDelete -> {
                val photos = albumFlow.value.files.filter { event.items.contains(it.uuid) }
                photoActionsChannel.trySend(PhotoAction.DeletePhotos(photos))
            }

            is AlbumDetailUiEvent.OnExport -> {
                val photos = albumFlow.value.files.filter { event.items.contains(it.uuid) }
                photoActionsChannel.trySend(PhotoAction.ExportPhotos(photos))
            }

            is AlbumDetailUiEvent.OpenPhoto -> {
                photoActionsChannel.trySend(PhotoAction.OpenPhoto(event.item.uuid, albumFlow.value.uuid))
            }

            AlbumDetailUiEvent.DeleteAlbum -> {
                viewModelScope.launch {
                    albumsRepository.deleteAlbum(albumFlow.value)
                        .onSuccess { navEventsChannel.trySend(AlbumDetailNavigator.NavigationEvent.Close) }
                        .onFailure {
                            navEventsChannel.trySend(
                                AlbumDetailNavigator.NavigationEvent.ShowToast(
                                    resources.getString(R.string.common_error)
                                )
                            )
                        }
                }
            }

            AlbumDetailUiEvent.OnImport -> photoActionsChannel.trySend(PhotoAction.OpenImportMenu)
            is AlbumDetailUiEvent.RemoveFromAlbum -> {
                viewModelScope.launch {
                    albumsRepository.unlink(event.items, albumFlow.value.uuid)
                    navEventsChannel.trySend(
                        AlbumDetailNavigator.NavigationEvent.ShowToast(
                            resources.getString(R.string.common_ok)
                        )
                    )
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted(ALBUM_DETAIL_UUID) albumUUID: String): AlbumDetailViewModel
    }
}
