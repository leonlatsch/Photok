/*
 *   Copyright 2020-2026 Leon Latsch
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

package dev.leonlatsch.photok.imageviewer.ui.compose

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import dev.leonlatsch.photok.imageviewer.ui.ImageViewerItem
import dev.leonlatsch.photok.imageviewer.ui.ImageViewerSystemBarsController
import dev.leonlatsch.photok.imageviewer.ui.ImageViewerUiEvent
import dev.leonlatsch.photok.imageviewer.ui.ImageViewerUiState
import dev.leonlatsch.photok.imageviewer.ui.ImageViewerViewModel
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(UnstableApi::class)
@Composable
fun ImageViewerScreen(
    navController: NavController,
    photoUuid: String,
    albumUuid: String?,
) {
    CompositionLocalProvider(
        LocalContentColor provides Color.White
    ) {
        val viewModel: ImageViewerViewModel =
            hiltViewModel<ImageViewerViewModel, ImageViewerViewModel.Factory>(
                creationCallback = { factory ->
                    factory.create(albumUuid)
                }
            )

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val handleUiEvent = viewModel::handleUiEvent

        val context = LocalContext.current

        val player = remember {
            ExoPlayer.Builder(context)
                .setMediaSourceFactory(viewModel.mediaSourceFactory)
                .build()
        }

        LaunchedEffect(player, uiState.loopVideos) {
            player.repeatMode = if (uiState.loopVideos) {
                ExoPlayer.REPEAT_MODE_ONE
            } else {
                ExoPlayer.REPEAT_MODE_OFF
            }
        }

        val pagerState = rememberPagerState { uiState.items.size }

        val currentItem by remember {
            derivedStateOf {
                uiState.items.getOrNull(pagerState.settledPage)
            }
        }

        LaunchedEffect(pagerState.settledPage, uiState.items) {
            val item = currentItem
            if (item is ImageViewerItem.Video) {
                player.apply {
                    setMediaItem(item.mediaItem)
                    prepare()
                    playWhenReady = true
                }
            } else {
                player.pause()
            }
        }

        LaunchedEffect(uiState.items.size) {
            if (uiState.items.isNotEmpty()) {
                val initial = uiState.items.find { it.photo.uuid == photoUuid }
                initial ?: return@LaunchedEffect

                pagerState.scrollToPage(
                    uiState.items.indexOf(initial)
                )
            }
        }



        val exoPlayerState = remember {
            ExoPlayerState()
        }

        DisposableEffect(player) {
            val listener = object : Player.Listener {

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    exoPlayerState.isPlaying = isPlaying
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    exoPlayerState.duration = player.duration.coerceAtLeast(0L)
                    exoPlayerState.playbackState = playbackState

                    if (playbackState == Player.STATE_ENDED) {
                        handleUiEvent(ImageViewerUiEvent.UpdateShowControls(true))
                    }
                }

                override fun onEvents(player: Player, events: Player.Events) {
                    if (!exoPlayerState.isScrubbing) {
                        exoPlayerState.position = player.currentPosition
                    }
                }
            }

            player.addListener(listener)

            onDispose {
                player.removeListener(listener)
                player.release()
            }
        }

        // Update state.position while playing
        LaunchedEffect(player, exoPlayerState.isScrubbing) {
            val delay = 1000L / 60 // 60 FPS controls update

            while (isActive && !exoPlayerState.isScrubbing) {
                exoPlayerState.position = player.currentPosition
                delay(delay)
            }
        }

        // Apply mute state to player
        LaunchedEffect(player, exoPlayerState.isMute) {
            if (exoPlayerState.isMute) {
                player.mute()
            } else {
                player.unmute()
            }
        }

        // Auto hide controls after 3 seconds of playing
        LaunchedEffect(exoPlayerState.isPlaying, uiState.showControls) {
            if (exoPlayerState.isPlaying && uiState.showControls) {
                delay(3000)
                if (isActive) {
                    handleUiEvent(ImageViewerUiEvent.UpdateShowControls(false))
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
        ) { pageIndex ->
            val item = uiState.items[pageIndex]
            ImageViewerPage(
                item = uiState.items[pageIndex],
                isCurrentItem = item.photo.uuid == currentItem?.photo?.uuid,
                player = player,
                exoPlayerState = exoPlayerState,
                uiState = uiState,
                handleUiEvent = handleUiEvent,
            )
        }


        ImageViewerControls(
            visible = uiState.showControls,
            currentItem = currentItem,
            handleUiEvent = handleUiEvent,
            uiState = uiState,
            navController = navController,
        )

        ImageViewerSystemBarsController(visible = uiState.showControls)
    }
}

@PreviewLightDark
@Composable
private fun ControlsPreview() {
    AppTheme() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Blue)
        ) {
            ImageViewerControls(
                visible = true,
                currentItem = ImageViewerItem.Image(
                    photo = Photo(
                        fileName = "Preview File aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                        importedAt = System.currentTimeMillis(),
                        type = PhotoType.JPEG,
                        size = 512L,
                        lastModified = null,
                    )
                ),
                uiState = ImageViewerUiState(),
                handleUiEvent = {},
                navController = rememberNavController(),
            )
        }
    }
}