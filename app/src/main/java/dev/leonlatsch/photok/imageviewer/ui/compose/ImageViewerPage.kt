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

package dev.leonlatsch.photok.imageviewer.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.ui.compose.ContentFrame
import dev.leonlatsch.photok.imageloading.compose.model.EncryptedImageRequestData
import dev.leonlatsch.photok.imageloading.compose.rememberEncryptedImagePainter
import dev.leonlatsch.photok.imageviewer.ui.ImageViewerItem
import dev.leonlatsch.photok.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.EnabledZoomGestures
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable

@Composable
fun ImageViewerPage(
    item: ImageViewerItem,
    player: Player,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(Color.Black)
    ) {
        when (item) {
            is ImageViewerItem.Image -> ImagePage(
                item = item,
                onClick = onClick,
            )

            is ImageViewerItem.Video -> VideoPage(
                item = item,
                player = player,
                onClick = onClick,
            )
        }
    }
}

@Composable
private fun ImagePage(
    item: ImageViewerItem.Image,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val photo = item.photo

    val requestData = remember(photo) {
        val fileName = if (photo.type.isVideo) {
            photo.internalVideoPreviewFileName
        } else {
            photo.internalFileName
        }

        EncryptedImageRequestData(
            internalFileName = fileName,
            mimeType = photo.type.mimeType,
            playAnimation = true,
        )
    }

    Image(
        painter = rememberEncryptedImagePainter(
            data = requestData,
            placeholder = android.R.color.black,
        ),
        contentDescription = photo.fileName,
        modifier = modifier
            .fillMaxSize()
            .zoomable(
                onClick = { onClick() },
                state = rememberZoomableState(zoomSpec = ZoomSpec(maxZoomFactor = 4f)),
                gestures = EnabledZoomGestures.ZoomAndPan,
            ),
    )
}

@Composable
private fun VideoPage(
    item: ImageViewerItem.Video,
    player: Player,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VideoPageContent(
        onClick = onClick,
        player = player,
        modifier = modifier,
    )
}

@Stable
private class PlaybackUiState {
    var position by mutableLongStateOf(0L)
    var duration by mutableLongStateOf(0L)
    var isPlaying by mutableStateOf(false)
    var isScrubbing by mutableStateOf(false)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoPageContent(
    onClick: () -> Unit,
    player: Player?,
    modifier: Modifier = Modifier,
) {
    val state = remember { PlaybackUiState() }

    DisposableEffect(player) {
        if (player == null) return@DisposableEffect onDispose { }

        val listener = object : Player.Listener {

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                state.isPlaying = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                state.duration = player.duration.coerceAtLeast(0L)
            }

            override fun onEvents(player: Player, events: Player.Events) {
                if (!state.isScrubbing) {
                    state.position = player.currentPosition
                }
            }
        }

        player.addListener(listener)

        onDispose {
            player.removeListener(listener)
        }
    }

    LaunchedEffect(player, state.isScrubbing) {
        if (player == null) return@LaunchedEffect

        val delay = 1000L / 60 // 60 FPS controls update

        while (isActive && !state.isScrubbing) {
            state.position = player.currentPosition
            delay(delay)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (player != null) {
            ContentFrame(
                player = player,
                modifier = Modifier.zoomable(
                    onClick = { onClick() },
                    state = rememberZoomableState(zoomSpec = ZoomSpec(maxZoomFactor = 4f)),
                    gestures = EnabledZoomGestures.ZoomAndPan,
                ),
            )

            val colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.onPrimary,
                activeTrackColor = MaterialTheme.colorScheme.onPrimary,
                inactiveTrackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
            )

            Slider(
                value = state.position.toFloat(),
                onValueChange = {
                    player.pause()
                    state.isScrubbing = true
                    state.position = it.toLong()
                },
                onValueChangeFinished = {
                    player.seekTo(state.position)
                    player.play()
                    state.isScrubbing = false
                },
                valueRange = 0f..player.duration.coerceAtLeast(minimumValue = 0L).toFloat(),
                colors = colors,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 70.dp)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}
