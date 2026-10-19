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

package dev.leonlatsch.photok.model.io

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.request.videoFramePercent
import coil.size.Precision
import coil.size.Scale
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.io.VaultCacheStorage
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.model.database.entity.internalFileName
import dev.leonlatsch.photok.model.database.entity.internalVideoPreviewFileName
import dev.leonlatsch.photok.transcoding.compose.model.EncryptedImageRequestData
import dev.leonlatsch.photok.transcoding.di.EncryptedImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

private const val THUMBNAIL_SIZE = 720
private const val THUMBNAIL_QUALITY = 80

private const val MAX_PARALLEL_CREATIONS_FROM_VAULT = 2
private const val LOCK_STRIPES = 64

sealed interface CreatedThumbnail {
    data class New(val bitmap: Bitmap) : CreatedThumbnail
    data object Existing : CreatedThumbnail
}

/**
 * Creates thumbnails in the cache dir. Never creates the same thumbnail twice at the same time.
 *
 * A thumbnail that could not be created from the vault is not tried again until [forgetFailures].
 */
@Singleton
class ThumbnailGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
    @EncryptedImageLoader private val encryptedImageLoader: Lazy<ImageLoader>,
    private val imageLoader: ImageLoader,
    private val vaultCacheStorage: VaultCacheStorage,
) {

    private val locks = List(LOCK_STRIPES) { Mutex() }
    private val creationsFromVault = Semaphore(MAX_PARALLEL_CREATIONS_FROM_VAULT)
    private val failedUuids = ConcurrentHashMap.newKeySet<String>()

    /**
     * Creates the thumbnail of an imported photo from its encrypted original. Videos use their video preview.
     */
    suspend fun createFromVault(uuid: String, type: PhotoType): Result<CreatedThumbnail> =
        lockFor(uuid).withLock {
            if (vaultCacheStorage.encryptedFileExists(ThumbnailFiles.path(uuid))) {
                return@withLock Result.success(CreatedThumbnail.Existing)
            }

            if (uuid in failedUuids) {
                return@withLock Result.failure(IllegalStateException("Creating the thumbnail of $uuid failed before"))
            }

            creationsFromVault.withPermit {
                val sourceFileName = if (type.isVideo) {
                    internalVideoPreviewFileName(uuid)
                } else {
                    internalFileName(uuid)
                }

                val request = ImageRequest.Builder(context)
                    .data(
                        EncryptedImageRequestData.VaultFile(
                            internalFileName = sourceFileName,
                            mimeType = type.mimeType,
                        )
                    )
                    .thumbnailSize()
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .build()

                decode(encryptedImageLoader.get(), request).mapCatching { bitmap ->
                    write(uuid, bitmap)
                    CreatedThumbnail.New(bitmap)
                }.onFailure {
                    failedUuids += uuid
                }
            }
        }

    fun forgetFailures() {
        failedUuids.clear()
    }

    /**
     * Creates the thumbnail of a photo while importing it.
     *
     * @param data The data for the photo. May be ByteArray or system Uri
     */
    suspend fun createFromImport(uuid: String, data: Any?, isVideo: Boolean): Result<Unit> =
        lockFor(uuid).withLock {
            val request = ImageRequest.Builder(context)
                .data(data)
                .thumbnailSize()
                .apply { if (isVideo) videoFramePercent(0.5) }
                .build()

            decode(imageLoader, request).mapCatching { bitmap ->
                write(uuid, bitmap)
                bitmap.recycle()
            }
        }

    private fun lockFor(uuid: String): Mutex =
        locks[uuid.hashCode().mod(LOCK_STRIPES)]

    private fun ImageRequest.Builder.thumbnailSize() =
        size(THUMBNAIL_SIZE)
            .scale(Scale.FILL)
            .precision(Precision.EXACT)
            .allowHardware(false)

    private suspend fun decode(loader: ImageLoader, request: ImageRequest): Result<Bitmap> =
        when (val result = loader.execute(request)) {
            is SuccessResult -> runCatching { result.drawable.toBitmap() }
            is ErrorResult -> Result.failure(result.throwable)
        }

    private suspend fun write(uuid: String, bitmap: Bitmap) = withContext(Dispatchers.IO) {
        val tmpPath = ThumbnailFiles.tmpPath(uuid)
        val output = vaultCacheStorage.openEncryptedOutput(tmpPath)
            ?: error("Could not open $tmpPath")

        try {
            output.use { bitmap.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_QUALITY, it) }
        } catch (e: Exception) {
            vaultCacheStorage.deleteEncryptedFile(tmpPath)
            throw e
        }

        if (!vaultCacheStorage.renameEncryptedFile(tmpPath, ThumbnailFiles.path(uuid))) {
            vaultCacheStorage.deleteEncryptedFile(tmpPath)
            error("Could not rename $tmpPath")
        }
    }
}
