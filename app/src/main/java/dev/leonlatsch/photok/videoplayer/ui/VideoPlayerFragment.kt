package dev.leonlatsch.photok.videoplayer.ui

import android.os.Bundle
import android.view.View
import androidx.annotation.OptIn
import androidx.fragment.app.viewModels
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentVideoPlayerBinding
import dev.leonlatsch.photok.other.IntentParams
import dev.leonlatsch.photok.other.extensions.hideSystemUI
import dev.leonlatsch.photok.uicomponnets.bindings.BindableFragment

/**
 * Fragment to play videos.
 *
 * @since 1.3.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class VideoPlayerFragment :
    BindableFragment<FragmentVideoPlayerBinding>(R.layout.fragment_video_player) {

    private val viewModel: VideoPlayerViewModel by viewModels()

    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().hideSystemUI()

        binding.videoPlayerToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.addOnPropertyChange<ExoPlayer?>(BR.player) {
            it ?: return@addOnPropertyChange
            binding.playerView.player = it
        }

        binding.playerView.setControllerVisibilityListener(
            PlayerView.ControllerVisibilityListener { visibility ->
                binding.videoPlayerAppBarLayout.visibility = visibility
            }
        )

        binding.playerView.showController()

        val photoUUID = arguments?.getString(IntentParams.PHOTO_UUID)
        if (photoUUID == null) {
            findNavController().navigateUp()
            return
        }

        viewModel.setupPlayer(photoUUID)
    }

    override fun bind(binding: FragmentVideoPlayerBinding) {
        super.bind(binding)
        binding.context = this
    }
}