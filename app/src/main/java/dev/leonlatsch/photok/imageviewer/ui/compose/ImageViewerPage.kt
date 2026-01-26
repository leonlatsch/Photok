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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.ContentFrame
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.imageloading.compose.model.EncryptedImageRequestData
import dev.leonlatsch.photok.imageloading.compose.rememberEncryptedImagePainter
import dev.leonlatsch.photok.imageviewer.ui.ImageViewerItem
import dev.leonlatsch.photok.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import me.saket.telephoto.zoomable.EnabledZoomGestures
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import java.util.Locale

@Composable
fun ImageViewerPage(
    item: ImageViewerItem,
    player: Player,
    updateShowControls: (Boolean) -> Unit,
    showControls: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(Color.Black)
    ) {
        when (item) {
            is ImageViewerItem.Image -> ImagePage(
                item = item,
                updateShowControls = updateShowControls,
                showControls = showControls,
            )

            is ImageViewerItem.Video -> VideoPage(
                item = item,
                player = player,
                updateControlsVisible = updateShowControls,
                showControls = showControls,
            )
        }
    }
}

@Composable
private fun ImagePage(
    item: ImageViewerItem.Image,
    updateShowControls: (Boolean) -> Unit,
    showControls: Boolean,
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
                onClick = { updateShowControls(!showControls) },
                state = rememberZoomableState(zoomSpec = ZoomSpec(maxZoomFactor = 4f)),
                gestures = EnabledZoomGestures.ZoomAndPan,
            ),
    )
}

@Composable
private fun VideoPage(
    item: ImageViewerItem.Video,
    player: Player,
    updateControlsVisible: (Boolean) -> Unit,
    showControls: Boolean,
    modifier: Modifier = Modifier,
) {
    VideoPageContent(
        updateControlsVisible = updateControlsVisible,
        showControls = showControls,
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
    var isMute by mutableStateOf(false)
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoPageContent(
    updateControlsVisible: (Boolean) -> Unit,
    showControls: Boolean,
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
                if (playbackState == Player.STATE_ENDED) {
                    updateControlsVisible(true)
                }
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

    // Update state.position while playing
    LaunchedEffect(player, state.isScrubbing) {
        if (player == null) return@LaunchedEffect

        val delay = 1000L / 60 // 60 FPS controls update

        while (isActive && !state.isScrubbing) {
            state.position = player.currentPosition
            delay(delay)
        }
    }

    // Apply mute state to player
    LaunchedEffect(player, state.isMute) {
        if (state.isMute) {
            player?.mute()
        } else {
            player?.unmute()
        }
    }

    // Auto hide controls after 3 seconds of playing
    LaunchedEffect(state.isPlaying, showControls) {
        if (state.isPlaying && showControls) {
            delay(3000)
            if (isActive) {
                updateControlsVisible(false)
            }
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
                    onClick = { updateControlsVisible(!showControls) },
                    state = rememberZoomableState(zoomSpec = ZoomSpec(maxZoomFactor = 4f)),
                    gestures = EnabledZoomGestures.ZoomAndPan,
                ),
            )
        }

        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    AnimatedContent(
                        targetState = state.isPlaying,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                    ) { isPlaying ->
                        val icon = if (isPlaying) {
                            R.drawable.media3_icon_pause
                        } else {
                            R.drawable.media3_icon_play
                        }
                        IconButton(
                            onClick = {
                                if (isPlaying) player?.pause() else player?.play()
                            }
                        ) {
                            Icon(
                                painter = painterResource(icon),
                                contentDescription = if (isPlaying) "pause" else "play",
                            )
                        }
                    }

                    val formattedPosition = remember(state.position) {
                        state.position.toVideoTime()
                    }
                    val formattedDuration = remember(state.duration) {
                        state.duration.toVideoTime()
                    }

                    Text(
                        text = "$formattedPosition / $formattedDuration",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    AnimatedContent(
                        targetState = state.isMute,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                    ) { isMute ->
                        val icon = if (isMute) {
                            R.drawable.media3_icon_volume_off
                        } else {
                            R.drawable.media3_icon_volume_up
                        }
                        IconButton(
                            onClick = { state.isMute = !state.isMute }
                        ) {
                            Icon(
                                painter = painterResource(icon),
                                contentDescription = if (isMute) "unmute" else "mute",
                            )
                        }
                    }
                }

                val sliderColors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.onPrimary,
                    activeTrackColor = MaterialTheme.colorScheme.onPrimary,
                    inactiveTrackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                )

                Slider(
                    value = state.position.toFloat(),
                    onValueChange = {
                        player?.pause()
                        state.isScrubbing = true
                        state.position = it.toLong()
                    },
                    onValueChangeFinished = {
                        player?.seekTo(state.position)
                        player?.play()
                        state.isScrubbing = false
                    },
                    valueRange = 0f..state.duration.coerceAtLeast(minimumValue = 0L).toFloat(),
                    colors = sliderColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )
            }
        }
    }
}

fun Long.toVideoTime(): String {
    val totalSeconds = this / 1_000

    val seconds = (totalSeconds % 60)
    val minutes = (totalSeconds / 60) % 60
    val hours = (totalSeconds / 3_600)

    return when {
        hours > 0 -> String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        else -> String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }
}

@Preview
@Composable
private fun VideoControlsPreview() {
    AppTheme() {
        CompositionLocalProvider(
            LocalContentColor provides Color.White
        ) {
            VideoPageContent(
                updateControlsVisible = {},
                showControls = true,
                player = null,
            )
        }
    }
}
