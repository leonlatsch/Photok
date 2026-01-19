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
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.ContentFrame
import androidx.media3.ui.compose.material3.buttons.PlayPauseButton
import androidx.media3.ui.compose.material3.buttons.SeekBackButton
import androidx.media3.ui.compose.material3.buttons.SeekForwardButton
import dev.leonlatsch.photok.imageloading.compose.model.EncryptedImageRequestData
import dev.leonlatsch.photok.imageloading.compose.rememberEncryptedImagePainter
import dev.leonlatsch.photok.model.database.entity.Photo
import me.saket.telephoto.zoomable.EnabledZoomGestures
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable

@Composable
fun PhotoViewHolderContent(
    photo: Photo,
    onClick: () -> Unit,
    onPlayVideo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .background(Color.Black)
    ) {
        if (photo.type.isVideo) {
            var player: Player? by remember { mutableStateOf(null) }

            LifecycleStartEffect(photo) {
                player = ExoPlayer.Builder(context)
//                .setMediaSourceFactory() TODO
                    .build()
                    .apply {
                        // TODO: set media item
                        prepare()
                        playWhenReady = true // TODO: Make this configurable
                    }

                onStopOrDispose {
                    player?.release()
                    player = null
                }
            }

            ContentFrame(
                player = player,
            )

            player?.let {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                ) {
                    SeekBackButton(it)
                    PlayPauseButton(it)
                    SeekForwardButton(it)
                }
            }

//            Icon(
//                painter = painterResource(R.drawable.ic_play_circle),
//                contentDescription = stringResource(R.string.view_photo_play_button_description),
//                modifier = Modifier
//                    .align(Alignment.Center)
//                    .size(62.dp)
//                    .clickable { onPlayVideo() },
//                tint = Color.LightGray,
//            )

        } else {
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
                modifier = Modifier.zoomable(
                    onClick = { onClick() },
                    state = rememberZoomableState(zoomSpec = ZoomSpec(maxZoomFactor = 4f)),
                    gestures = if (photo.type.isVideo) EnabledZoomGestures.None else EnabledZoomGestures.ZoomAndPan,
                ),
            )
        }
    }
}