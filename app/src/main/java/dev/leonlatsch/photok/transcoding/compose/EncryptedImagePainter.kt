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

package dev.leonlatsch.photok.transcoding.compose

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.memory.MemoryCache
import coil.request.ImageRequest
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.transcoding.compose.model.EncryptedImageRequestData

/**
 * Image Painter for encrypted images. Uses encrypted image fetcher if [LocalEncryptedImageLoader] provides it
 */
@Composable
fun rememberEncryptedImagePainter(
    data: EncryptedImageRequestData,
    @DrawableRes placeholder: Int = R.color.lightGray,
    placeholderMemoryCacheKey: String? = null,
): AsyncImagePainter {
    val context = LocalContext.current

    return rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(data)
            .placeholder(placeholder)
            .placeholderMemoryCacheKey(placeholderMemoryCacheKey)
            .fallback(R.color.design_default_color_error)
            .error(R.color.design_default_color_error)
            .build(),
        imageLoader = LocalEncryptedImageLoader.current ?: LocalContext.current.imageLoader
    )
}

@Composable
fun cachedThumbnail(uuid: String): ImageBitmap? {
    val imageLoader = LocalEncryptedImageLoader.current ?: LocalContext.current.imageLoader
    val bitmap = imageLoader.memoryCache
        ?.get(MemoryCache.Key(EncryptedImageRequestData.Thumbnail.memoryCacheKey(uuid)))
        ?.bitmap
        ?: return null

    return remember(bitmap) { bitmap.asImageBitmap() }
}