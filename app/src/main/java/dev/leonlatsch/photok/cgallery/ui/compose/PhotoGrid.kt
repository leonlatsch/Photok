/*
 *   Copyright 2020-2023 Leon Latsch
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

package dev.leonlatsch.photok.cgallery.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.imageloading.compose.model.EncryptedImageRequestData
import dev.leonlatsch.photok.imageloading.compose.rememberEncryptedImagePainter
import dev.leonlatsch.photok.model.database.entity.Photo

@Composable
fun PhotosGrid(photos: List<Photo>, selectionMode: Boolean) {
    LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxWidth()) {
        items(photos, key = { it.uuid }) {
            GalleryPhotoTile(it, selectionMode)
        }
    }
}

private val VideoIconSize = 20.dp

@Composable
private fun GalleryPhotoTile(photo: Photo, selectionMode: Boolean) {
    Box(modifier = Modifier.padding(.5.dp)) {
        val contentModifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)

        if (LocalInspectionMode.current) {
            Box(
                modifier = contentModifier.background(Color.Red)
            )
        } else {
            val requestData = remember(photo) {
                EncryptedImageRequestData(
                    photo.internalThumbnailFileName,
                    photo.type.mimeType
                )
            }

            Image(
                painter = rememberEncryptedImagePainter(requestData),
                contentDescription = photo.fileName,
                modifier = contentModifier
            )
        }

        if (photo.type.isVideo) {
            Icon(
                painter = painterResource(R.drawable.ic_videocam),
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier
                    .padding(2.dp)
                    .size(VideoIconSize)
                    .align(Alignment.BottomStart)
            )
        }

        // TODO: Move to ui state
        var selected by remember { mutableStateOf(false) }

        if (selectionMode) {
            Checkbox(
                checked = selected,
                onCheckedChange = { selected = !selected },
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}