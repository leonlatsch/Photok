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
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.imageviewer.data.video.AesDataSource
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.sort.domain.SortConfig
import dev.leonlatsch.photok.sort.domain.SortRepository
import dev.leonlatsch.photok.uicomponnets.bindings.ObservableViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

sealed interface ImageViewerUiEvent {
    data class ConfirmDelete(val item: ImageViewerItem) : ImageViewerUiEvent
    data class ConfirmExport(
        val item: ImageViewerItem,
        val target: Uri,
        val context: Context,
    ) : ImageViewerUiEvent
    data class UpdateLoopVideos(val newValue: Boolean) : ImageViewerUiEvent
    data class UpdateShowControls(val newValue: Boolean) : ImageViewerUiEvent
    data object ToggleShowControls : ImageViewerUiEvent
    data object ToggleMuteVideoPlayer : ImageViewerUiEvent
    data class UpdateCurrentDialog(val newValue: ImageViewerUiState.Dialog?) : ImageViewerUiEvent
}

data class ImageViewerUiState(
    val items: List<ImageViewerItem> = emptyList(),
    val loopVideos: Boolean = false,
    val muteVideoPlayer: Boolean = false,
    val inputs: Inputs = Inputs(),
) {
    data class Inputs(
        val showControls: Boolean = false,
        val currentDialog: Dialog? = null,
    )

    enum class Dialog {
        ConfirmDelete,
        ConfirmExport,
        MoreMenu,
        AlbumPicker,
    }
}

const val ALBUM_UUID = "albumUuid"

/**
 * ViewModel for loading the full size photo to [ViewPhotoActivity].
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@OptIn(UnstableApi::class)
@HiltViewModel(assistedFactory = ImageViewerViewModel.Factory::class)
class ImageViewerViewModel @AssistedInject constructor(
    @Assisted(ALBUM_UUID) private val albumUuid: String?,
    private val app: Application,
    private val encryptionManager: EncryptionManager,
    private val photoRepository: PhotoRepository,
    private val albumRepository: AlbumRepository,
    private val sortRepository: SortRepository,
    private val config: Config,
) : ObservableViewModel(app) {

    private val inputs = MutableStateFlow(ImageViewerUiState.Inputs())

    val uiState = combine(
        createPhotosFlow(),
        config.valuesFlow,
        inputs,
    ) { photos, configValues, inputs ->
        ImageViewerUiState(
            items = photos.map { photo ->
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
            },
            loopVideos = configValues.getOrDefault(Config.IMAGE_VIEWER_LOOP_VIDEO, false) as Boolean,
            muteVideoPlayer = configValues.getOrDefault(Config.IMAGE_VIEWER_MUTE_VIDEO_PLAYER, false) as Boolean,
            inputs = inputs,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ImageViewerUiState())

    fun handleUiEvent(event: ImageViewerUiEvent) {
        when (event) {
            is ImageViewerUiEvent.ConfirmDelete -> viewModelScope.launch {
                photoRepository.safeDeletePhoto(event.item.photo)
            }

            is ImageViewerUiEvent.ConfirmExport -> viewModelScope.launch {
                photoRepository.exportPhoto(event.item.photo, event.target)
            }

            is ImageViewerUiEvent.UpdateLoopVideos -> viewModelScope.launch {
                config.imageViewerLoopVideos = event.newValue
            }

            is ImageViewerUiEvent.UpdateShowControls -> inputs.update {
                it.copy(showControls = event.newValue)
            }

            is ImageViewerUiEvent.ToggleShowControls -> inputs.update { old ->
                old.copy(showControls = !old.showControls)
            }

            is ImageViewerUiEvent.ToggleMuteVideoPlayer -> viewModelScope.launch {
                config.imageViewerMuteVideoPlayer = !uiState.value.muteVideoPlayer
            }

            is ImageViewerUiEvent.UpdateCurrentDialog -> inputs.update {
                it.copy(currentDialog = event.newValue)
            }

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

    private fun createPhotosFlow(): Flow<List<Photo>> {
        val sort = runBlocking {
            sortRepository.getSortForAlbum(albumUuid) ?: SortConfig.defaultFor(albumUuid)
        }

        return if (albumUuid == null) {
            photoRepository.observeAll(sort)
        } else {
            albumRepository.observeAlbumWithPhotos(
                uuid = albumUuid,
                sort = sort
            ).map { it.files }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted(ALBUM_UUID) albumUuid: String?
        ): ImageViewerViewModel
    }
}