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

package dev.leonlatsch.photok.imageviewer.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.other.onMain
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.settings.ui.compose.PreferenceView
import dev.leonlatsch.photok.uicomponnets.bindings.ObservableViewModel
import dev.leonlatsch.photok.videoplayer.data.AesDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ImageViewerUiEvent {
    data class ConfirmDelete(val item: ImageViewerItem) : ImageViewerUiEvent
    data class ConfirmExport(
        val item: ImageViewerItem,
        val target: Uri,
        val context: Context,
    ) : ImageViewerUiEvent
}

/**
 * ViewModel for loading the full size photo to [ViewPhotoActivity].
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@OptIn(UnstableApi::class)
@HiltViewModel
class ImageViewerViewModel @Inject constructor(
    private val app: Application,
    private val encryptionManager: EncryptionManager,
    val photoRepository: PhotoRepository,
    val albumRepository: AlbumRepository,
) : ObservableViewModel(app) {


    val items = MutableStateFlow<List<ImageViewerItem>>(emptyList())

    fun handleUiEvent(event: ImageViewerUiEvent) {
        when (event) {
            is ImageViewerUiEvent.ConfirmDelete -> viewModelScope.launch {
                photoRepository.safeDeletePhoto(event.item.photo)
            }

            is ImageViewerUiEvent.ConfirmExport -> viewModelScope.launch {
                photoRepository.exportPhoto(event.item.photo, event.target)
            }
        }
    }

    fun loadItems(albumUuid: String) { // TODO: Make this observe and map to uiState
        viewModelScope.launch {
            val mappedItems = if (albumUuid.isEmpty()) {
                photoRepository.findAllPhotosByImportDateDesc()
            } else {
                albumRepository.getPhotosForAlbum(albumUuid)
            }.map { photo ->
                if (photo.type.isVideo) {
                    ImageViewerItem.Video(
                        photo = photo,
                        mediaItem = createMediaItem(photo)
                    )
                } else {
                    ImageViewerItem.Image(
                        photo = photo
                    )
                }
            }

            items.update { mappedItems }
        }
    }

    private fun createMediaItem(photo: Photo): MediaItem {
        val uri = Uri.fromFile(app.getFileStreamPath(photo.internalFileName).canonicalFile)

        return MediaItem.Builder()
            .setMimeType(photo.type.mimeType)
            .setUri(uri)
            .build()
    }

    val mediaSourceFactory: MediaSource.Factory by lazy {
        val aesDataSource = AesDataSource(
            encryptionManager = encryptionManager,
        )

        val factory = DataSource.Factory {
            aesDataSource
        }

        ProgressiveMediaSource.Factory(factory)
    }

    // LEGACY

    var photos = listOf<Photo>()

    @get:Bindable
    var currentPhoto: Photo? = null
        set(value) {
            field = value
            notifyChange(BR.currentPhoto, value)
        }

    /**
     * Load all photo Ids.
     * Save them in viewModel and pass them to [onFinished].
     */
    fun preloadData(
        albumUUID: String,
        onFinished: (List<Photo>) -> Unit
    ) = viewModelScope.launch {
        if (photos.isNotEmpty()) {
            onFinished(photos)
            return@launch
        }

        photos = if (albumUUID.isEmpty()) {
            photoRepository.findAllPhotosByImportDateDesc()
        } else {
            albumRepository.getPhotosForAlbum(albumUUID)
        }

        onFinished(photos)
    }
}