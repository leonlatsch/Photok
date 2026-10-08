/*
 *   Copyright 2020-2026 Leon Latsch
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

package dev.leonlatsch.photok.transcoding.data

import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.graphics.drawable.toDrawable
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import dev.leonlatsch.photok.io.VaultCacheStorage
import dev.leonlatsch.photok.model.io.CreatedThumbnail
import dev.leonlatsch.photok.model.io.ThumbnailFiles
import dev.leonlatsch.photok.model.io.ThumbnailGenerator
import dev.leonlatsch.photok.transcoding.compose.model.EncryptedImageRequestData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.buffer
import okio.source
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

private const val THUMBNAIL_MIME_TYPE = "image/jpeg"

/**
 * Loads a thumbnail from the cache dir. A missing thumbnail is created right away and returned without reading it back.
 * An unreadable thumbnail is deleted and created again.
 */
class ThumbnailFetcher(
    private val vaultCacheStorage: VaultCacheStorage,
    private val thumbnailGenerator: ThumbnailGenerator,
    private val requestData: EncryptedImageRequestData.Thumbnail,
    private val context: Context,
) : Fetcher {

    override suspend fun fetch(): FetchResult? = withContext(Dispatchers.IO) {
        val path = ThumbnailFiles.path(requestData.uuid)

        if (vaultCacheStorage.encryptedFileExists(path)) {
            val inputStream = vaultCacheStorage.openEncryptedInput(path)
            inputStream ?: return@withContext null

            val bytes = readDecodableBytes(inputStream)
            if (bytes != null) {
                return@withContext sourceResult(bytes)
            }

            Timber.w("Deleting unreadable thumbnail of ${requestData.uuid}")
            vaultCacheStorage.deleteEncryptedFile(path)
        }

        val created = thumbnailGenerator.createFromVault(requestData.uuid, requestData.type)
            .getOrElse {
                Timber.w(it, "Could not create thumbnail for ${requestData.uuid}")
                return@withContext null
            }

        when (created) {
            is CreatedThumbnail.New -> DrawableResult(
                drawable = created.bitmap.toDrawable(context.resources),
                isSampled = true,
                dataSource = DataSource.DISK,
            )

            is CreatedThumbnail.Existing -> {
                val inputStream = vaultCacheStorage.openEncryptedInput(path)
                inputStream ?: return@withContext null

                readDecodableBytes(inputStream)?.let { sourceResult(it) }
            }
        }
    }

    private suspend fun readDecodableBytes(inputStream: InputStream): ByteArray? {
        val bytes = try {
            inputStream.use { it.readBytesSuspending() }
        } catch (e: IOException) {
            Timber.w(e, "Could not read thumbnail of ${requestData.uuid}")
            return null
        }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)

        return bytes.takeIf { bounds.outWidth > 0 && bounds.outHeight > 0 }
    }

    private fun sourceResult(bytes: ByteArray) = SourceResult(
        source = ImageSource(
            source = ByteArrayInputStream(bytes).source().buffer(),
            context = context,
        ),
        mimeType = THUMBNAIL_MIME_TYPE,
        dataSource = DataSource.DISK,
    )
}
