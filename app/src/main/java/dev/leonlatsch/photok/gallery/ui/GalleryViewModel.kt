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

package dev.leonlatsch.photok.gallery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.gallery.ui.components.PhotoTile
import dev.leonlatsch.photok.gallery.ui.navigation.GalleryNavigationEvent
import dev.leonlatsch.photok.gallery.ui.navigation.PhotoAction
import dev.leonlatsch.photok.imageloading.di.EncryptedImageLoader
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.news.newfeatures.ui.FEATURE_VERSION_CODE
import dev.leonlatsch.photok.settings.data.Config
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
class GalleryViewModel @Inject constructor(
    photoRepository: PhotoRepository,
    @EncryptedImageLoader val encryptedImageLoader: ImageLoader,
    private val galleryUiStateFactory: GalleryUiStateFactory,
    private val config: Config,
    private val albumRepository: AlbumRepository
) : ViewModel() {

    private val photosFlow = photoRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, listOf())

    private val showAlbumSelectionDialog = MutableStateFlow(false)

    val uiState: StateFlow<GalleryUiState> = combine(
        photosFlow,
        showAlbumSelectionDialog,
    ) { photos, showAlbumSelection ->
        galleryUiStateFactory.create(photos, showAlbumSelection)
    }.stateIn(viewModelScope, SharingStarted.Lazily, GalleryUiState.Empty)

    private val eventsChannel = Channel<GalleryNavigationEvent>()
    val eventsFlow = eventsChannel.receiveAsFlow()

    private val photoActionsChannel = Channel<PhotoAction>()
    val photoActions = photoActionsChannel.receiveAsFlow()

    fun handleUiEvent(event: GalleryUiEvent) {
        when (event) {
            is GalleryUiEvent.OpenImportMenu -> photoActionsChannel.trySend(PhotoAction.OpenImportMenu)
            is GalleryUiEvent.OpenPhoto -> navigateToPhoto(event.item)
            is GalleryUiEvent.OnDelete -> onDeleteSelectedItems(event.items)
            is GalleryUiEvent.OnExport -> onExportSelectedItems(event.items)
            is GalleryUiEvent.OnAddToAlbum -> showAlbumSelectionDialog.value = true
            is GalleryUiEvent.OnAlbumSelected -> onAlbumSelected(event.photoIds, event.albumId)
            GalleryUiEvent.CancelAlbumSelection -> showAlbumSelectionDialog.value = false
        }
    }

    private fun onAlbumSelected(items: List<String>, albumId: String) {
        viewModelScope.launch {
            items.forEach { photoId ->
                albumRepository.linkPhotoToAlbum(photoId, albumId)
            }
        }
        showAlbumSelectionDialog.value = false
    }

    private fun onExportSelectedItems(selectedItems: List<String>) {
        photoActionsChannel.trySend(
            PhotoAction.ExportPhotos(
                photosFlow.value.filter { selectedItems.contains(it.uuid) }
            )
        )
    }

    private fun onDeleteSelectedItems(selectedItems: List<String>) {
        photoActionsChannel.trySend(
            PhotoAction.DeletePhotos(
                photosFlow.value.filter { selectedItems.contains(it.uuid) }
            )
        )
    }

    private fun navigateToPhoto(item: PhotoTile) {
        photoActionsChannel.trySend(PhotoAction.OpenPhoto(item.uuid))
    }

    fun checkForNewFeatures() = viewModelScope.launch {
        if (config.systemLastFeatureVersionCode >= FEATURE_VERSION_CODE) return@launch

        eventsChannel.trySend(GalleryNavigationEvent.ShowNewFeaturesDialog)
        config.systemLastFeatureVersionCode = FEATURE_VERSION_CODE
    }
}

