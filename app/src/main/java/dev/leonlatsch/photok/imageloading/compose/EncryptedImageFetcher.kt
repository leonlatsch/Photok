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

import android.graphics.ImageDecoder
import android.graphics.Movie
import android.os.Build
import android.os.Build.VERSION_CODES
import coil3.asImage
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.fetch.SourceFetchResult
import coil3.gif.MovieDrawable
import dev.leonlatsch.photok.imageloading.compose.model.EncryptedImageRequestData
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileHandle
import okio.FileMetadata
import okio.FileSystem
import okio.Path
import okio.Sink
import okio.Source
import okio.buffer
import okio.source
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
) : Fetcher {

    override suspend fun fetch(): FetchResult? = withContext(Dispatchers.IO) {
        val inputStream =
            encryptedStorageManager.internalOpenEncryptedFileInput(requestData.internalFileName)
        inputStream ?: return@withContext null

        if (requestData.mimeType == PhotoType.GIF.mimeType && requestData.playGif) {
            val drawable = decodeGif(inputStream)
            ImageFetchResult(
                image = drawable.asImage(),
                isSampled = false,
                dataSource = DataSource.DISK,
            )
        } else {
            SourceFetchResult(
                source = ImageSource(
                    source = inputStream.source().buffer(),
                    fileSystem = FileSystem.SYSTEM,
                    metadata = null,
                ),
                mimeType = null,
                dataSource = DataSource.DISK,
            )
        }
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