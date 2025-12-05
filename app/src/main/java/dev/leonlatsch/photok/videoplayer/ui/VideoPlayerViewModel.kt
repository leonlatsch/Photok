/*
 *   Copyright 2020-2021 Leon Latsch
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

package dev.leonlatsch.photok.videoplayer.ui

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
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.other.onMain
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.uicomponnets.bindings.ObservableViewModel
import dev.leonlatsch.photok.videoplayer.data.AesDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for playing videos.
 *
 * @since 1.3.0
 * @author Leon Latsch
 */
@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    private val app: Application,
    private val photoRepository: PhotoRepository,
    private val encryptionManager: EncryptionManager,
) : ObservableViewModel(app) {

    @get:Bindable
    var player: ExoPlayer? = null
        set(value) {
            field = value
            notifyChange(BR.player, value)
        }

    /**
     * Create and prepare the [player] to play the passed video.
     */
    fun setupPlayer(photoUUID: String) {
        releasePlayer()

        viewModelScope.launch(Dispatchers.IO) {
            val photo = photoRepository.get(photoUUID)

            player = ExoPlayer.Builder(app)
                .setMediaSourceFactory(createMediaSourceFactory())
                .build()
                .apply {
                    onMain {
                        setMediaItem(createMediaItem(photo))
                        prepare()
                        playWhenReady = true
                    }
                }
        }
    }

    @OptIn(UnstableApi::class)
    private fun createMediaSourceFactory(): MediaSource.Factory {
        val aesDataSource = AesDataSource(
            encryptionManager = encryptionManager,
        )

        val factory = DataSource.Factory {
            aesDataSource
        }

        return ProgressiveMediaSource.Factory(factory)
    }

    private fun createMediaItem(photo: Photo): MediaItem {
        val uri = Uri.fromFile(app.getFileStreamPath(photo.internalFileName).canonicalFile)

        return MediaItem.Builder()
            .setMimeType(photo.type.mimeType)
            .setUri(uri)
            .build()
    }

    /**
     * Release the current player
     */
    fun releasePlayer() {
        player?.release()
        player = null
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}