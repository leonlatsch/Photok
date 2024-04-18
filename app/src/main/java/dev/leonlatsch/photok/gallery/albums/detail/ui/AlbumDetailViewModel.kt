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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.gallery.ui.components.PhotoTile
import dev.leonlatsch.photok.imageloading.di.EncryptedImageLoader
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

const val ALBUM_DETAIL_UUID = "album_uuid"

@HiltViewModel(assistedFactory = AlbumDetailViewModel.Factory::class)
class AlbumDetailViewModel @AssistedInject constructor(
    @Assisted(ALBUM_DETAIL_UUID) private val albumUUID: String,
    private val albumsRepository: AlbumRepository,
    @EncryptedImageLoader val encryptedImageLoader: ImageLoader,
) : ViewModel() {

    val uiState = albumsRepository.getAlbum(albumUUID).map { album ->
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

    fun handleUiEvent(event: AlbumDetailUiEvent) {
        when (event) {
            is AlbumDetailUiEvent.ImportIntoAlbum -> TODO()
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted(ALBUM_DETAIL_UUID) albumUUID: String): AlbumDetailViewModel
    }
}

data class AlbumDetailUiState(
    val albumName: String = "",
    val photos: List<PhotoTile> = emptyList()
)

