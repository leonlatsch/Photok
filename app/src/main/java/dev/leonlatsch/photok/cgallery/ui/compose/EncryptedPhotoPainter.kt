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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import dev.leonlatsch.photok.BaseApplication
import dev.leonlatsch.photok.model.database.entity.Photo

@Composable
fun rememberEncryptedPhotoThumbnailPainter(photo: Photo): AsyncImagePainter {
    val context = LocalContext.current
    val photoRepository = remember { (context.applicationContext as BaseApplication).photoRepository }

    val photoBytes: MutableState<ByteArray?> = remember { mutableStateOf(ByteArray(0)) }

    LaunchedEffect(photo) {
        photoBytes.value = photoRepository.loadThumbnail(photo)
    }

    return rememberAsyncImagePainter(photoBytes.value)
}