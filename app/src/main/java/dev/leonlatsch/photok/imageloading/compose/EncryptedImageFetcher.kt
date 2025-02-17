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
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Movie
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Build.VERSION_CODES
import android.view.WindowManager
import androidx.core.graphics.drawable.toDrawable
import coil.decode.DataSource
import coil.drawable.MovieDrawable
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import dev.leonlatsch.photok.imageloading.compose.model.EncryptedImageRequestData
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import dev.leonlatsch.photok.other.extensions.getCompatScreenSize
import java.io.InputStream
import javax.crypto.CipherInputStream

/**
 * Coil image fetcher decrypting the image on the fly while rendering.
 *
 * Used for displaying encrypted images.
 */
class EncryptedImageFetcher(
    private val encryptedStorageManager: EncryptedStorageManager,
    private val requestData: EncryptedImageRequestData,
    private val resources: Resources,
    private val windowManager: WindowManager,
) : Fetcher {

    @Suppress("DEPRECATION") // ImageDecoder is only available from API 28
    override suspend fun fetch(): FetchResult? {
        val inputStream = encryptedStorageManager.internalOpenEncryptedFileInput(requestData.internalFileName)
        inputStream ?: return null

        val drawable = if (requestData.mimeType == PhotoType.GIF.mimeType && requestData.playGif) {
            decodeGif(inputStream)
        } else {
            safeDecodeInputStream(inputStream) ?: return null
        }

        return DrawableResult(
            drawable = drawable,
            isSampled = false,
            dataSource = DataSource.DISK,
        )
    }

    private fun decodeGif(inputStream: CipherInputStream) =
        if (Build.VERSION.SDK_INT >= VERSION_CODES.S) {
            val bytes = inputStream.readBytes()
            val source = ImageDecoder.createSource(bytes)
            ImageDecoder.decodeDrawable(source)
        } else {
            val movie = Movie.decodeStream(inputStream)
            MovieDrawable(movie)
        }

    private fun safeDecodeInputStream(inputStream: InputStream): BitmapDrawable? {
        val rawBytes = inputStream.use { it.readBytes() }

        val (screenWidth, screenHeight) = windowManager.getCompatScreenSize()
        val (imageWidth, imageHeight) = getDimensionsFromBytes(rawBytes)

        val sampleSize = calculateSampleSize(imageWidth, imageHeight, screenWidth, screenHeight)

        val bitmapOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }

        val bitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, bitmapOptions)
        return bitmap?.toDrawable(resources)
    }
    
    private fun getDimensionsFromBytes(rawBytes: ByteArray): Pair<Int, Int> {
        val onlyDecodeBoundsOption = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, onlyDecodeBoundsOption)
        
        return onlyDecodeBoundsOption.run { outWidth to outHeight }
    }

    private fun calculateSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}