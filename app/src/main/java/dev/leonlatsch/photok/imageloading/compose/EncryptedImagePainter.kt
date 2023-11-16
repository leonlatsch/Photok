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

package dev.leonlatsch.photok.imageloading.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import dev.leonlatsch.photok.BaseApplication
import dev.leonlatsch.photok.imageloading.compose.model.EncryptedImageRequestData

@Composable
fun rememberEncryptedImagePainter(data: EncryptedImageRequestData): AsyncImagePainter {
    val context = LocalContext.current
    val encryptedStorageManager =
        remember { (context.applicationContext as BaseApplication).encryptedStorageManager }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(EncryptedImageFetcherFactory(context, encryptedStorageManager))
            }
            .build()
    }

    return rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(data)
            .build(),
        imageLoader = imageLoader
    )
}