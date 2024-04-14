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

import android.content.res.Configuration
import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.gallery.ui.components.PhotoTile
import dev.leonlatsch.photok.gallery.ui.navigation.GalleryNavigationEvent
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

private const val PORTRAIT_COLUMN_COUNT = 3
private const val LANDSCAPE_COLUMN_COUNT = 6

@HiltViewModel
class GalleryViewModel @Inject constructor(
    photoRepository: PhotoRepository,
    @EncryptedImageLoader val encryptedImageLoader: ImageLoader,
    private val galleryUiStateFactory: GalleryUiStateFactory,
    private val config: Config,
    private val resources: Resources,
) : ViewModel() {

    private val photosFlow = photoRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, listOf())

    private val columnCountFlow = MutableStateFlow(PORTRAIT_COLUMN_COUNT)

    val uiState: StateFlow<GalleryUiState> = combine(
        photosFlow,
        columnCountFlow
    ) { photos, columnCount ->
        galleryUiStateFactory.create(photos, columnCount)
    }.stateIn(viewModelScope, SharingStarted.Lazily, GalleryUiState.Empty)

    private val eventsChannel = Channel<GalleryNavigationEvent>()
    val eventsFlow = eventsChannel.receiveAsFlow()

    fun handleUiEvent(event: GalleryUiEvent) {
        when (event) {
            is GalleryUiEvent.OpenImportMenu -> eventsChannel.trySend(GalleryNavigationEvent.OpenImportMenu)
            is GalleryUiEvent.OpenPhoto -> navigateToPhoto(event.item)
            is GalleryUiEvent.OnDelete -> onDeleteSelectedItems(event.items)
            is GalleryUiEvent.OnExport -> onExportSelectedItems(event.items)
        }
    }

    private fun onExportSelectedItems(selectedItems: List<String>) {
        eventsChannel.trySend(
            GalleryNavigationEvent.StartExportDialog(
                photosFlow.value.filter { selectedItems.contains(it.uuid) })
        )
    }

    private fun onDeleteSelectedItems(selectedItems: List<String>) {
        eventsChannel.trySend(GalleryNavigationEvent.StartDeleteDialog(
            photosFlow.value.filter { selectedItems.contains(it.uuid) }
        ))
    }

    private fun navigateToPhoto(item: PhotoTile) {
        eventsChannel.trySend(GalleryNavigationEvent.OpenPhoto(item.uuid))
    }

    fun checkForNewFeatures() = viewModelScope.launch {
        if (config.systemLastFeatureVersionCode >= FEATURE_VERSION_CODE) return@launch

        eventsChannel.trySend(GalleryNavigationEvent.ShowNewFeaturesDialog)
        config.systemLastFeatureVersionCode = FEATURE_VERSION_CODE
    }

    fun onConfigurationChanged() {
        columnCountFlow.value = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> PORTRAIT_COLUMN_COUNT
            Configuration.ORIENTATION_LANDSCAPE -> LANDSCAPE_COLUMN_COUNT
            else -> PORTRAIT_COLUMN_COUNT
        }
    }
}

