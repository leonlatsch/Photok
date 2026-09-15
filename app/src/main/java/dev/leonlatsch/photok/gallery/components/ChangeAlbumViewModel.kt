/*
 *   Copyright 2020–2026 Leon Latsch
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

package dev.leonlatsch.photok.gallery.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.gallery.albums.toUi
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.transcoding.compose.model.EncryptedImageRequestData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ChangeAlbumUiEvent {
    data class ToggleAlbum(val albumUuid: String) : ChangeAlbumUiEvent
    data object Save : ChangeAlbumUiEvent
}

const val PHOTO_UUID = "photoUuid"

@HiltViewModel(assistedFactory = ChangeAlbumViewModel.Factory::class)
class ChangeAlbumViewModel @AssistedInject constructor(
    @Assisted(PHOTO_UUID) private val photoUuid: String,
    private val albumRepository: AlbumRepository,
    private val photoRepository: PhotoRepository,
    private val appScope: CoroutineScope,
) : ViewModel() {

    private val selectedAlbumUuids = MutableStateFlow<Set<String>?>(null)

    private var initialSelection: Set<String> = emptySet()

    private val photoFlow = flow { emit(photoRepository.get(photoUuid)) }

    val uiState: StateFlow<ChangeAlbumUiState> = combine(
        albumRepository.observeAllAlbumsWithPhotos(),
        selectedAlbumUuids,
        photoFlow,
    ) { albums, selected, photo ->
        if (selected == null || photo == null) {
            return@combine ChangeAlbumUiState.Loading
        }

        ChangeAlbumUiState.Content(
            thumbnail = photo.toThumbnailRequestData(),
            albums = albums.map { album ->
                ChangeAlbumItem(
                    album = album.toUi(),
                    selected = album.uuid in selected,
                )
            },
            hasChanges = selected != initialSelection,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ChangeAlbumUiState.Loading)

    init {
        viewModelScope.launch(IO) {
            initialSelection = albumRepository.getAlbumUUIDsForPhoto(photoUuid)
            selectedAlbumUuids.value = initialSelection
        }
    }

    fun handleUiEvent(event: ChangeAlbumUiEvent) {
        when (event) {
            is ChangeAlbumUiEvent.ToggleAlbum -> selectedAlbumUuids.update { selected ->
                val current = selected ?: return@update selected

                if (event.albumUuid in current) {
                    current - event.albumUuid
                } else {
                    current + event.albumUuid
                }
            }

            ChangeAlbumUiEvent.Save -> {
                val selected = selectedAlbumUuids.value ?: return
                val added = selected - initialSelection
                val removed = initialSelection - selected

                appScope.launch(IO) {
                    added.forEach { albumRepository.link(listOf(photoUuid), it) }
                    removed.forEach { albumRepository.unlink(listOf(photoUuid), it) }
                }
            }
        }
    }

    private fun Photo.toThumbnailRequestData() = EncryptedImageRequestData(
        internalFileName = internalThumbnailFileName,
        mimeType = type.mimeType,
    )

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted(PHOTO_UUID) photoUuid: String
        ): ChangeAlbumViewModel
    }
}
