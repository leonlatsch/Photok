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

package dev.leonlatsch.photok.ui.videoplayer

import android.app.Application
import android.net.Uri
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.other.onMain
import dev.leonlatsch.photok.ui.components.bindings.ObservableViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject

/**
 * ViewModel for playing videos.
 *
 * @since 2.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    private val app: Application,
    private val photoRepository: PhotoRepository,
    private val encryptedStorageManager: EncryptedStorageManager
) : ObservableViewModel(app) {

    private var tmpFile: File? = null
    private var tmpInput: FileInputStream? = null

    @get:Bindable
    var player: SimpleExoPlayer? = null
        set(value) {
            field = value
            notifyChange(BR.player, value)
        }

    fun setupPlayer(photoId: Int) = viewModelScope.launch(Dispatchers.IO) {
        val photo = photoRepository.get(photoId)

        player = SimpleExoPlayer.Builder(app).build()
        player!!.apply {
            onMain {
                setMediaSource(createMediaSource(photo))
                prepare()
                playWhenReady = true
            }
        }
    }

    private fun createMediaSource(photo: Photo): MediaSource {
        val dataSource = EncryptedDataSource(encryptedStorageManager, photo.internalFileName)

        val factory = DataSource.Factory {
            dataSource
        }

        return ProgressiveMediaSource.Factory(factory)
            .createMediaSource(MediaItem.fromUri(Uri.EMPTY))
    }

    fun closePlayer() = viewModelScope.launch(Dispatchers.IO) {
        player?.release()
        player = null
        tmpInput?.close()
        tmpFile?.delete()
    }
}