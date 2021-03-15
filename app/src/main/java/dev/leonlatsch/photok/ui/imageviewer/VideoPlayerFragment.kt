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

package dev.leonlatsch.photok.ui.imageviewer

import android.net.Uri
import android.os.Bundle
import android.view.View
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.ByteArrayDataSource
import com.google.android.exoplayer2.upstream.DataSource
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentVideoPlayerBinding
import dev.leonlatsch.photok.other.INTENT_VIDEO_BYTES
import dev.leonlatsch.photok.ui.components.bindings.BindableFragment

/**
 * Fragment to play videos.
 *
 * @since 2.0.0
 * @author Leon Latsch
 */
class VideoPlayerFragment :
    BindableFragment<FragmentVideoPlayerBinding>(R.layout.fragment_video_player) {

    override fun bind(binding: FragmentVideoPlayerBinding) {
        super.bind(binding)
        binding.context = this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val player = SimpleExoPlayer.Builder(requireContext())
            .setUseLazyPreparation(true)
            .build()

        binding.vieoPlayerView.player = player

        val videoBytes = arguments?.getByteArray(INTENT_VIDEO_BYTES)
        videoBytes ?: requireActivity().onBackPressed()

        player.setMediaSource(createVideoMediaSource(videoBytes!!))
        player.prepare()
        player.playWhenReady = true
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