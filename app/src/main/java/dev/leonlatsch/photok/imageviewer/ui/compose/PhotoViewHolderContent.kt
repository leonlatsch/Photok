/*
 *   Copyright 2020-2024 Leon Latsch
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.imageloading.compose.model.EncryptedImageRequestData
import dev.leonlatsch.photok.imageloading.compose.rememberEncryptedImagePainter
import dev.leonlatsch.photok.model.database.entity.Photo
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable

@Composable
fun PhotoViewHolderContent(
    photo: Photo,
    onClick: () -> Unit,
    onPlayVideo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier
        .background(Color.Black)) {
        val contentModifier = Modifier.fillMaxSize()
        if (LocalInspectionMode.current) {
            Box(modifier = contentModifier.background(Color.Red))
        } else {
            val requestData = remember(photo) {
                val fileName = if (photo.type.isVideo) {
                    photo.internalVideoPreviewFileName
                } else {
                    photo.internalFileName
                }

                EncryptedImageRequestData(
                    fileName,
                    photo.type.mimeType,
                )
            }

            Image(
                painter = rememberEncryptedImagePainter(
                    data = requestData,
                    placeholder = android.R.color.black,
                ),
                contentDescription = photo.fileName,
                modifier = contentModifier.zoomable(
                    onClick = { onClick() },
                    state =  rememberZoomableState(),
                    enabled = !photo.type.isVideo
                ),
            )
        }

        if (photo.type.isVideo) {
            Icon(
                painter = painterResource(R.drawable.ic_play_circle),
                contentDescription = stringResource(R.string.view_photo_play_button_description),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(62.dp)
                    .clickable { onPlayVideo() },
                tint = Color.LightGray,
            )
        }
    }
}