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

import android.media.MediaPlayer
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentVideoPlayerBinding
import dev.leonlatsch.photok.other.INTENT_PHOTO_ID
import dev.leonlatsch.photok.other.hide
import dev.leonlatsch.photok.other.show
import dev.leonlatsch.photok.ui.components.bindings.BindableFragment

/**
 * Fragment to play videos.
 *
 * @since 2.0.0
 * @author Leon Latsch
 */

@AndroidEntryPoint
class VideoPlayerFragment :
    BindableFragment<FragmentVideoPlayerBinding>(R.layout.fragment_video_player) {

    private val viewModel: VideoPlayerViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.videoPlayerBufferIndicator.show()

        binding.videoPlayerToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }


        val photoId = arguments?.get(INTENT_PHOTO_ID)
        if (photoId == null || photoId !is Int) {
            requireActivity().onBackPressed()
            return
        }

        binding.videoView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                viewModel.setupPlayer(photoId, holder)
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                viewModel.closePlayer()
            }
        })

        viewModel.addOnPropertyChange<MediaPlayer?>(BR.player) {
            binding.videoPlayerBufferIndicator.hide()
        }
    }

    override fun bind(binding: FragmentVideoPlayerBinding) {
        super.bind(binding)
        binding.context = this
    }
}