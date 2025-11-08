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
import dev.leonlatsch.photok.gallery.albums.detail.ui.AlbumDetailNavigator.NavigationEvent.ShowToast
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.gallery.albums.domain.model.Album
import dev.leonlatsch.photok.gallery.ui.components.ImportChoice
import dev.leonlatsch.photok.gallery.ui.components.PhotoTile
import dev.leonlatsch.photok.gallery.ui.navigation.PhotoAction
import dev.leonlatsch.photok.gallery.ui.navigation.PhotoAction.DeletePhotos
import dev.leonlatsch.photok.gallery.ui.navigation.PhotoAction.ExportPhotos
import dev.leonlatsch.photok.gallery.ui.navigation.PhotoAction.OpenPhoto
import dev.leonlatsch.photok.sort.domain.SortConfig
import dev.leonlatsch.photok.sort.domain.SortRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

const val ALBUM_DETAIL_UUID = "album_uuid"

@HiltViewModel(assistedFactory = AlbumDetailViewModel.Factory::class)
class AlbumDetailViewModel @AssistedInject constructor(
    @Assisted(ALBUM_DETAIL_UUID) private val albumUUID: String,
    private val albumsRepository: AlbumRepository,
    private val sortRepository: SortRepository,
    private val resources: Resources,
) : ViewModel() {

    private val sortFlow = sortRepository.observeSortFor(albumUuid = albumUUID, default = SortConfig.Album.default)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val albumFlow = sortFlow.flatMapLatest { sort ->
        albumsRepository.observeAlbumWithPhotos(albumUUID, sort)
    }.stateIn(viewModelScope, SharingStarted.Lazily, Album("", "", emptyList()))

    private val photoActionsChannel = Channel<PhotoAction>()
    val photoActions = photoActionsChannel.receiveAsFlow()

    val uiState = combine(
        albumFlow,
        sortFlow,
    ) { album, sort ->
        AlbumDetailUiState(
            albumId = album.uuid,
            albumName = album.name,
            photos = album.files.map {
                PhotoTile(
                    it.internalThumbnailFileName,
                    it.type,
                    it.uuid
                )
            },
            sort = sort,
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, AlbumDetailUiState())


    private val navEventsChannel = Channel<AlbumDetailNavigator.NavigationEvent>()
    val navEvents = navEventsChannel.receiveAsFlow()

    fun handleUiEvent(event: AlbumDetailUiEvent) {
        when (event) {
            is AlbumDetailUiEvent.OnDelete -> {
                val photos = albumFlow.value.files.filter { event.items.contains(it.uuid) }
                photoActionsChannel.trySend(DeletePhotos(photos))
            }

            is AlbumDetailUiEvent.OnExport -> {
                if (event.target != null) {
                    val photos = albumFlow.value.files.filter { event.items.contains(it.uuid) }
                    photoActionsChannel.trySend(ExportPhotos(photos, event.target))
                }
            }

            is AlbumDetailUiEvent.OpenPhoto -> {
                photoActionsChannel.trySend(
                    OpenPhoto(
                        event.item.uuid,
                        albumFlow.value.uuid
                    )
                )
            }

            AlbumDetailUiEvent.DeleteAlbum -> {
                viewModelScope.launch {
                    albumsRepository.deleteAlbum(albumFlow.value)
                        .onSuccess {
                            navEventsChannel.trySend(
                                ShowToast(
                                    resources.getString(R.string.gallery_albums_deleted)
                                )
                            )
                            navEventsChannel.trySend(AlbumDetailNavigator.NavigationEvent.Close)
                        }
                        .onFailure {
                            navEventsChannel.trySend(
                                ShowToast(
                                    resources.getString(R.string.common_error)
                                )
                            )
                        }
                }
            }

            is AlbumDetailUiEvent.RemoveFromAlbum -> {
                viewModelScope.launch {
                    albumsRepository.unlink(event.items, albumFlow.value.uuid)
                    navEventsChannel.trySend(
                        ShowToast(
                            resources.getString(R.string.common_ok)
                        )
                    )
                }
            }

            is AlbumDetailUiEvent.RenameAlbum -> renameAlbum(event.newName)
            is AlbumDetailUiEvent.OnImportChoice -> onImportChoice(event.choice)
            is AlbumDetailUiEvent.SortChanged -> viewModelScope.launch {
                sortRepository.updateSortFor(albumUuid = albumUUID, sort = event.sort)
            }
        }
    }

    private fun onImportChoice(choice: ImportChoice) {
        val navEvent = when (choice) {
            is ImportChoice.AddNewFiles -> AlbumDetailNavigator.NavigationEvent.StartImport(
                fileUris = choice.fileUris,
                albumUuid = albumFlow.value.uuid,
            )

            is ImportChoice.RestoreBackup -> AlbumDetailNavigator.NavigationEvent.StartRestoreBackup(
                choice.backupUri,
            )
        }

        navEventsChannel.trySend(navEvent)
    }

    private fun renameAlbum(newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            albumsRepository.rename(
                albumUUID = albumFlow.value.uuid,
                newName = newName,
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted(ALBUM_DETAIL_UUID) albumUUID: String): AlbumDetailViewModel
    }
}
