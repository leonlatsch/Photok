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

package dev.leonlatsch.photok.cgallery.ui

import android.content.res.Configuration
import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.cgallery.ui.navigation.GalleryNavigationEvent
import dev.leonlatsch.photok.imageloading.di.EncryptedImageLoader
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.news.newfeatures.ui.FEATURE_VERSION_CODE
import dev.leonlatsch.photok.other.onMain
import dev.leonlatsch.photok.settings.data.Config
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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

    private val multiSelectionState =
        MutableStateFlow(MultiSelectionState(isActive = false, listOf()))

    private val columnCountFlow = MutableStateFlow(PORTRAIT_COLUMN_COUNT)

    val uiState: StateFlow<GalleryUiState> = combine(
        photosFlow,
        multiSelectionState,
        columnCountFlow
    ) { photos, multiSelectionState, columnCount ->
        galleryUiStateFactory.create(photos, multiSelectionState, columnCount)
    }.stateIn(viewModelScope, SharingStarted.Lazily, GalleryUiState.Empty)

    private val eventsChannel = Channel<GalleryNavigationEvent>()
    val eventsFlow = eventsChannel.receiveAsFlow()

    fun handleUiEvent(event: GalleryUiEvent) {
        when (event) {
            is GalleryUiEvent.OpenImportMenu -> eventsChannel.trySend(GalleryNavigationEvent.OpenImportMenu)
            is GalleryUiEvent.PhotoClicked -> onPhotoClicked(event.item)
            is GalleryUiEvent.PhotoLongPressed -> onPhotoLongPressed(event.item)
            is GalleryUiEvent.CancelMultiSelect -> onCancelMultiSelect()
            is GalleryUiEvent.OnDelete -> onDeleteSelectedItems()
            is GalleryUiEvent.OnExport -> onExportSelectedItems()
            is GalleryUiEvent.SelectAll -> onSelectAll()
        }
    }

    private fun onSelectAll() {
        multiSelectionState.update {
            it.copy(
                isActive = true,
                selectedItemUUIDs = photosFlow.value.map { photo -> photo.uuid })
        }
    }

    private fun onExportSelectedItems() {
        val uuidsToExport = multiSelectionState.value.selectedItemUUIDs
        eventsChannel.trySend(
            GalleryNavigationEvent.StartExportDialog(
                photosFlow.value.filter { uuidsToExport.contains(it.uuid) })
        )
        onCancelMultiSelect()
    }

    private fun onDeleteSelectedItems() {
        val uuidsToDelete = multiSelectionState.value.selectedItemUUIDs
        eventsChannel.trySend(GalleryNavigationEvent.StartDeleteDialog(
            photosFlow.value.filter { uuidsToDelete.contains(it.uuid) }
        ))
        onCancelMultiSelect()
    }

    private fun onCancelMultiSelect() {
        multiSelectionState.update { it.copy(isActive = false, selectedItemUUIDs = emptyList()) }
    }

    private fun onPhotoLongPressed(item: PhotoTile) {
        if (multiSelectionState.value.isActive.not()) {
            multiSelectionState.update {
                it.copy(
                    isActive = true,
                    selectedItemUUIDs = listOf(item.uuid)
                )
            }
        }
    }

    private fun onPhotoClicked(item: PhotoTile) {
        if (multiSelectionState.value.isActive.not()) {
            eventsChannel.trySend(GalleryNavigationEvent.OpenPhoto(item.uuid))
        } else {
            if (multiSelectionState.value.selectedItemUUIDs.contains(item.uuid)) {
                // Remove
                multiSelectionState.update {
                    it.copy(
                        isActive = it.selectedItemUUIDs.size != 1,
                        selectedItemUUIDs = it.selectedItemUUIDs.filterNot { selectedUUid ->
                            selectedUUid == item.uuid
                        },
                    )
                }
            } else {
                // Add
                multiSelectionState.update {
                    it.copy(
                        selectedItemUUIDs = it.selectedItemUUIDs + listOf(item.uuid)
                    )
                }
            }
        }
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

