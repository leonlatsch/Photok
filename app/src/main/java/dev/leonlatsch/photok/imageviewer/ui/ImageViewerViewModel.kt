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

    fun loadItems(albumUuid: String) {
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

    /**
     * Loads a photo. Gets called after onViewCreated
     */
    fun updateDetails(position: Int) = viewModelScope.launch {
        currentPhoto = photos[position]
    }

    /**
     * Deletes a single photo. Called after verification.
     *
     * @param onSuccess Block called on success
     * @param onError Block called on error
     */
    fun deletePhoto(onSuccess: () -> Unit, onError: () -> Unit) =
        viewModelScope.launch(Dispatchers.IO) {
            currentPhoto ?: return@launch

            photoRepository.safeDeletePhoto(currentPhoto!!).let {
                onMain {
                    if (it) onSuccess() else onError()
                }
            }
        }

    /**
     * Exports a single photo. Called after verification.
     *
     * @param onSuccess Block called on success
     * @param onError Block called on error
     */
    fun exportPhoto(target: Uri, onSuccess: () -> Unit, onError: () -> Unit) =
        viewModelScope.launch(Dispatchers.IO) {
            currentPhoto?.let { safeCurrentPhoto ->
                photoRepository.exportPhoto(safeCurrentPhoto, target).let { success ->
                    onMain {
                        if (success) onSuccess() else onError()
                    }
                }
            }

        }
}