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

@file:OptIn(ExperimentalLayoutApi::class)

package dev.leonlatsch.photok.imageviewer.ui.compose

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.WindowInsetsSides.Companion
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.ContentFrame
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.imageloading.compose.model.EncryptedImageRequestData
import dev.leonlatsch.photok.imageloading.compose.rememberEncryptedImagePainter
import dev.leonlatsch.photok.imageviewer.ui.ImageViewerItem
import dev.leonlatsch.photok.imageviewer.ui.ImageViewerUiEvent
import dev.leonlatsch.photok.imageviewer.ui.ImageViewerUiState
import me.saket.telephoto.zoomable.EnabledZoomGestures
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import java.util.Locale

@Composable
fun ImageViewerPage(
    item: ImageViewerItem,
    isCurrentItem: Boolean,
    player: Player,
    exoPlayerState: ExoPlayerState,
    uiState: ImageViewerUiState,
    handleUiEvent: (ImageViewerUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(Color.Black)
    ) {
        when (item) {
            is ImageViewerItem.Image -> ImagePage(
                uiState = uiState,
                handleUiEvent = handleUiEvent,
                item = item,
            )

            is ImageViewerItem.Video -> VideoPage(
                item = item,
                isCurrentItem = isCurrentItem,
                uiState = uiState,
                handleUiEvent = handleUiEvent,
                player = player,
                exoPlayerState = exoPlayerState,
            )
        }
    }
}

@Composable
private fun BoxScope.ImagePage(
    item: ImageViewerItem.Image,
    uiState: ImageViewerUiState,
    handleUiEvent: (ImageViewerUiEvent) -> Unit,
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
                onClick = {
                    handleUiEvent(
                        ImageViewerUiEvent.UpdateShowControls(!uiState.inputs.showControls)
                    )
                },
                state = rememberZoomableState(zoomSpec = ZoomSpec(maxZoomFactor = 4f)),
                gestures = EnabledZoomGestures.ZoomAndPan,
            ),
    )

    TopGradient(visible = uiState.inputs.showControls)
    BottomGradient(visible = uiState.inputs.showControls)
}

@Composable
private fun BoxScope.VideoPage(
    item: ImageViewerItem.Video,
    isCurrentItem: Boolean,
    exoPlayerState: ExoPlayerState,
    player: Player,
    uiState: ImageViewerUiState,
    handleUiEvent: (ImageViewerUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    ContentFrame(
        player = if (isCurrentItem) player else null,
        surfaceType = SURFACE_TYPE_TEXTURE_VIEW, // Somehow cures a weird issue with small amount of black screen on start
        shutter = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                val photo = item.photo

                val requestData = remember(photo) {

                    EncryptedImageRequestData(
                        internalFileName = photo.internalVideoPreviewFileName,
                        mimeType = photo.type.mimeType,
                        playAnimation = false,
                    )
                }

                Image(
                    painter = rememberEncryptedImagePainter(
                        data = requestData,
                        placeholder = android.R.color.black,
                    ),
                    contentDescription = photo.fileName,
                    modifier = modifier.fillMaxSize()
                )

                CircularProgressIndicator()
            }
        },
        modifier = modifier
            .fillMaxSize()
            .zoomable(
                onClick = {
                    handleUiEvent(ImageViewerUiEvent.ToggleShowControls)
                },
                state = rememberZoomableState(zoomSpec = ZoomSpec(maxZoomFactor = 4f)),
                gestures = EnabledZoomGestures.ZoomAndPan,
            ),
    )

    TopGradient(visible = uiState.inputs.showControls)
    BottomVideoGradient(visible = uiState.inputs.showControls)

    val navBarHeight = WindowInsets
        .navigationBarsIgnoringVisibility
        .asPaddingValues()
        .calculateBottomPadding()

    val sliderOffset = if (LocalConfiguration.current.orientation == ORIENTATION_LANDSCAPE) {
        20.dp
    } else {
        110.dp
    }

    AnimatedVisibility(
        visible = uiState.inputs.showControls,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(
                WindowInsets.displayCutout
                    .only(WindowInsetsSides.Horizontal)
                    .asPaddingValues()
            )
            .padding(bottom = navBarHeight + sliderOffset)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                AnimatedContent(
                    targetState = exoPlayerState.isPlaying,
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
                            if (isPlaying) {
                                player.pause()
                            } else {
                                if (exoPlayerState.playbackState == Player.STATE_ENDED) {
                                    player.seekTo(0L)
                                }
                                player.play()
                            }
                        },
                        enabled = exoPlayerState.availableCommands.contains(ExoPlayer.COMMAND_PLAY_PAUSE)
                    ) {
                        Icon(
                            painter = painterResource(icon),
                            contentDescription = if (isPlaying) "pause" else "play",
                        )
                    }
                }

                val formattedPosition = remember(exoPlayerState.position) {
                    exoPlayerState.position.toVideoTime()
                }
                val formattedDuration = remember(exoPlayerState.duration) {
                    exoPlayerState.duration.toVideoTime()
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
                    targetState = uiState.muteVideoPlayer,
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
                        onClick = {
                            handleUiEvent(ImageViewerUiEvent.ToggleMuteVideoPlayer)
                        },
                        enabled = exoPlayerState.availableCommands.contains(ExoPlayer.COMMAND_SET_VOLUME)
                    ) {
                        Icon(
                            painter = painterResource(icon),
                            contentDescription = if (isMute) "unmute" else "mute",
                        )
                    }
                }
            }

            val sliderColors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.4f)
            )

            Slider(
                value = exoPlayerState.position.toFloat(),
                onValueChange = {
                    player.pause()
                    exoPlayerState.isScrubbing = true
                    exoPlayerState.position = it.toLong()
                },
                onValueChangeFinished = {
                    player.seekTo(exoPlayerState.position)
                    player.play()
                    exoPlayerState.isScrubbing = false
                },
                valueRange = 0f..exoPlayerState.duration.coerceAtLeast(minimumValue = 0L)
                    .toFloat(),
                colors = sliderColors,
                enabled = exoPlayerState.availableCommands.contains(ExoPlayer.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )
        }

    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BoxScope.TopGradient(visible: Boolean) {
    val statusBarsHeight = WindowInsets
        .statusBarsIgnoringVisibility
        .asPaddingValues()
        .calculateTopPadding()


    val brush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.6f),
                Color.Transparent,
            )
        )
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.TopCenter)

    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(TopAppBarDefaults.TopAppBarExpandedHeight + statusBarsHeight + 40.dp)
                .background(brush)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BoxScope.BottomGradient(visible: Boolean) {
    val navBarHeight = WindowInsets
        .navigationBarsIgnoringVisibility
        .asPaddingValues()
        .calculateBottomPadding()

    val brush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color.Black.copy(alpha = 0.6f),
            )
        )
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.BottomCenter)

    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp + navBarHeight + 40.dp)
                .background(brush)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BoxScope.BottomVideoGradient(visible: Boolean) {
    val navBarHeight = WindowInsets
        .navigationBarsIgnoringVisibility
        .asPaddingValues()
        .calculateBottomPadding()

    val brush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color.Black.copy(alpha = 0.4f),
                Color.Black.copy(alpha = 0.6f),
            )
        )
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.BottomCenter)

    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp + navBarHeight + 180.dp)
                .background(brush)
        )
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
