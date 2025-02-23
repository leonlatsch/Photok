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

import android.content.Context
import android.graphics.ImageDecoder
import android.graphics.Movie
import android.os.Build
import android.os.Build.VERSION_CODES
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.drawable.MovieDrawable
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import dev.leonlatsch.photok.imageloading.compose.model.EncryptedImageRequestData
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import okio.BufferedSource
import okio.buffer
import okio.source
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Coil image fetcher decrypting the image on the fly while rendering.
 *
 * Used for displaying encrypted images.
 */
class EncryptedImageFetcher(
    private val encryptedStorageManager: EncryptedStorageManager,
    private val requestData: EncryptedImageRequestData,
    private val context: Context,
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val inputStream =
            encryptedStorageManager.internalOpenEncryptedFileInput(requestData.internalFileName)
        inputStream ?: return null

        return if (requestData.mimeType == PhotoType.GIF.mimeType && requestData.playGif) {
            val drawable = decodeGif(inputStream)
            DrawableResult(
                drawable = drawable,
                isSampled = false,
                dataSource = DataSource.DISK,
            )
        } else {
            SourceResult(
                source = ImageSource(
                    source = inputStream.inMemoryBufferedSource(),
                    context = context,
                ),
                mimeType = requestData.mimeType,
                DataSource.MEMORY,
            )
        }
    }

    private suspend fun InputStream.inMemoryBufferedSource(): BufferedSource {
        val rawBytes = this.use { it.readBytesSuspending() }
        val byteStream = ByteArrayInputStream(rawBytes)

        return byteStream.source().buffer()
    }

    private suspend fun decodeGif(inputStream: InputStream) =
        if (Build.VERSION.SDK_INT >= VERSION_CODES.S) {
            val bytes = inputStream.use { it.readBytesSuspending() }
            val source = ImageDecoder.createSource(bytes)
            ImageDecoder.decodeDrawable(source)
        } else {
            @Suppress("DEPRECATION")
            val movie = Movie.decodeStream(inputStream)
            MovieDrawable(movie)
        }
}

suspend fun InputStream.readBytesSuspending(): ByteArray = suspendCoroutine { continuation ->
    try {
        val bytes = readBytes()
        continuation.resume(bytes)
    } catch (e: Exception) {
        continuation.resumeWithException(e)
    }
}