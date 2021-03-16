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
import com.google.android.exoplayer2.upstream.ByteArrayDataSource
import com.google.android.exoplayer2.upstream.DataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.ui.components.bindings.ObservableViewModel
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
    private val app: Application
) : ObservableViewModel(app) {

    var videoBytes: ByteArray? = null

    @get:Bindable
    var player: SimpleExoPlayer? = null
        set(value) {
            field = value
            notifyChange(BR.player, value)
        }

    override fun setup() {
        super.setup()
        videoBytes ?: return

        viewModelScope.launch {
            val newPlayer = SimpleExoPlayer.Builder(app)
                .setUseLazyPreparation(true)
                .build().apply {
                    setMediaSource(createVideoMediaSource(videoBytes!!))
                    prepare()
                    playWhenReady = true
                }
            player = newPlayer
        }
    }

    private fun createVideoMediaSource(bytes: ByteArray): MediaSource {
        val dataSource = ByteArrayDataSource(bytes)

        val factory = DataSource.Factory {
            dataSource
        }

        return ProgressiveMediaSource.Factory(factory)
            .createMediaSource(MediaItem.fromUri(Uri.EMPTY))
    }
}