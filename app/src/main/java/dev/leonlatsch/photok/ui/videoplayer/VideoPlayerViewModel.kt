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
import android.media.MediaPlayer
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.ui.components.bindings.ObservableViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for playing videos.
 *
 * @since 2.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    app: Application,
    private val photoRepository: PhotoRepository,
    private val encryptedStorageManager: EncryptedStorageManager
) : ObservableViewModel(app) {

    @get:Bindable
    var player: MediaPlayer? = null
        set(value) {
            field = value
            notifyChange(BR.player, value)
        }

    fun setupPlayer(photoId: Int) = viewModelScope.launch(Dispatchers.IO) {
        val photo = photoRepository.get(photoId)

        val videoInput =
            encryptedStorageManager.internalOpenEncryptedFileInput(photo.internalFileName)
        videoInput ?: return@launch

        player = MediaPlayer().apply {
            setDataSource(EncryptedVideoMediaDataSource(videoInput))
            prepare()
            start()
        }
    }

    fun closePlayer() = viewModelScope.launch(Dispatchers.IO) {
        player?.release()
        player = null
    }
}