/*
 *   Copyright 2020-2023 Leon Latsch
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

package dev.leonlatsch.photok.cgallery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.cgallery.ui.navigation.GalleryNavigationEvent
import dev.leonlatsch.photok.imageloading.di.EncryptedImageLoader
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val photoRepository: PhotoRepository,
    @EncryptedImageLoader val encryptedImageLoader: ImageLoader
) : ViewModel() {

    val uiState = photoRepository.observeAll().map { photos ->
        if (photos.isEmpty()) {
            GalleryUiState.Empty
        } else {
            GalleryUiState.Content(
                selectionMode = false,
                photos = photos.map { PhotoTile(it.fileName, it.type, it.uuid) }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, GalleryUiState.Empty)

    private val eventsChannel = Channel<GalleryNavigationEvent>()
    val eventsFlow = eventsChannel.receiveAsFlow()

    fun handleUiEvent(event: GalleryUiEvent) {
        when (event) {
            is GalleryUiEvent.OpenImportMenu -> TODO()
            is GalleryUiEvent.OpenPhoto -> openPhoto(event)
        }
    }

    private fun openPhoto(event: GalleryUiEvent.OpenPhoto) {
        val photoId = uiState.value
        eventsChannel.trySend(GalleryNavigationEvent.OpenPhoto(event.item.uuid))
    }
}

