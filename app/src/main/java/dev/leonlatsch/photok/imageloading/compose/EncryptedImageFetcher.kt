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

package dev.leonlatsch.photok.imageloading.compose

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Movie
import android.os.Build
import androidx.core.graphics.drawable.toDrawable
import coil.decode.DataSource
import coil.drawable.MovieDrawable
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import dev.leonlatsch.photok.imageloading.compose.model.EncryptedImageRequestData
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.model.io.EncryptedStorageManager

/**
 * Coil image fetcher decrypting the image on the fly while rendering.
 *
 * Used for displaying encrypted images.
 */
class EncryptedImageFetcher(
    private val encryptedStorageManager: EncryptedStorageManager,
    private val requestData: EncryptedImageRequestData,
    private val resources: Resources,
) : Fetcher {

    @Suppress("DEPRECATION") // ImageDecoder is only available from API 28
    override suspend fun fetch(): FetchResult? {
        val inputStream = encryptedStorageManager.internalOpenEncryptedFileInput(requestData.internalFileName)
        inputStream ?: return null

        val drawable = if (requestData.mimeType == PhotoType.GIF.mimeType && requestData.playGif) {
            if (Build.VERSION.SDK_INT >= 31) {
                val bytes = inputStream.readBytes()
                val source = ImageDecoder.createSource(bytes)
                ImageDecoder.decodeDrawable(source)
            } else {
                val movie = Movie.decodeStream(inputStream)
                MovieDrawable(movie)
            }
        } else {
            val bitmap: Bitmap? = BitmapFactory.decodeStream(inputStream)
            bitmap ?: return null
            bitmap.toDrawable(resources)
        }

        return DrawableResult(
            drawable = drawable,
            isSampled = false,
            dataSource = DataSource.DISK,
        )
    }
}