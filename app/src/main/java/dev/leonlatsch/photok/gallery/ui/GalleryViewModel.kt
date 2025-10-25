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

import android.content.res.Resources
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.gallery.sort.domain.Sort
import dev.leonlatsch.photok.gallery.ui.components.ImportChoice
import dev.leonlatsch.photok.gallery.ui.components.PhotoTile
import dev.leonlatsch.photok.gallery.ui.navigation.GalleryNavigationEvent
import dev.leonlatsch.photok.gallery.ui.navigation.PhotoAction
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.news.newfeatures.ui.FEATURE_VERSION_CODE
import dev.leonlatsch.photok.settings.data.Config
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMap
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    photoRepository: PhotoRepository,
    private val galleryUiStateFactory: GalleryUiStateFactory,
    private val config: Config,
    private val albumRepository: AlbumRepository,
    private val resources: Resources,
) : ViewModel() {

    // TODO: Persist sort (prefs)
    private val sortFlow = MutableStateFlow(Sort.Default)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val photosFlow = sortFlow.flatMapLatest { sort ->
        photoRepository.observeAll(sort)
    }
        .onEach {
            Timber.d("$it")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), listOf())

    private val showAlbumSelectionDialog = MutableStateFlow(false)

    val uiState: StateFlow<GalleryUiState> = combine(
        photosFlow,
        showAlbumSelectionDialog,
        sortFlow,
    ) { photos, showAlbumSelection, sort ->
        galleryUiStateFactory.create(photos, showAlbumSelection, sort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), GalleryUiState.Empty)

    private val eventsChannel = Channel<GalleryNavigationEvent>()
    val eventsFlow = eventsChannel.receiveAsFlow()

    private val photoActionsChannel = Channel<PhotoAction>()
    val photoActions = photoActionsChannel.receiveAsFlow()

    fun handleUiEvent(event: GalleryUiEvent) {
        when (event) {
            is GalleryUiEvent.OpenPhoto -> navigateToPhoto(event.item)
            is GalleryUiEvent.OnDelete -> onDeleteSelectedItems(event.items)
            is GalleryUiEvent.OnExport -> onExportSelectedItems(event.items, event.target)
            is GalleryUiEvent.OnAddToAlbum -> showAlbumSelectionDialog.value = true
            is GalleryUiEvent.OnAlbumSelected -> addPhotosToSelectedAlbum(event.photoIds, event.albumId)
            GalleryUiEvent.CancelAlbumSelection -> showAlbumSelectionDialog.value = false
            is GalleryUiEvent.OnImportChoice -> onImportChoice(event.choice)
            is GalleryUiEvent.SortChanged -> sortFlow.update { event.sort }
        }
    }

    private fun onImportChoice(choice: ImportChoice) {
        val navEvent = when (choice) {
            is ImportChoice.AddNewFiles -> GalleryNavigationEvent.StartImport(choice.fileUris)
            is ImportChoice.RestoreBackup -> GalleryNavigationEvent.StartRestoreBackup(choice.backupUri)
        }

        eventsChannel.trySend(navEvent)
    }

    private fun addPhotosToSelectedAlbum(items: List<String>, albumId: String) {
        viewModelScope.launch {
            albumRepository.link(items, albumId)
        }
        showAlbumSelectionDialog.value = false
        eventsChannel.trySend(
            GalleryNavigationEvent.ShowToast(
                resources.getString(R.string.gallery_albums_photos_added, items.size)
            )
        )
    }

    private fun onExportSelectedItems(selectedItems: List<String>, target: Uri?) {
        target ?: return
        photoActionsChannel.trySend(
            PhotoAction.ExportPhotos(
                photosFlow.value.filter { selectedItems.contains(it.uuid) },
                target,
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

