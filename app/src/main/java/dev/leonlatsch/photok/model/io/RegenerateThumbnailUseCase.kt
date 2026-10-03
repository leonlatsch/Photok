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
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Scale
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.io.VaultFileStorage
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.transcoding.compose.model.EncryptedImageRequestData
import dev.leonlatsch.photok.transcoding.di.EncryptedImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TMP_SUFFIX = ".tmp"

/**
 * Creates a new thumbnail for an already imported photo from its encrypted full size file.
 * Videos use their video preview as source.
 */
class RegenerateThumbnailUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    @EncryptedImageLoader private val encryptedImageLoader: ImageLoader,
    private val vaultFileStorage: VaultFileStorage,
    private val photoRepository: PhotoRepository,
) {

    suspend operator fun invoke(photo: Photo): Result<Unit> = withContext(Dispatchers.IO) {
        val sourceFileName = if (photo.type.isVideo) {
            photo.internalVideoPreviewFileName
        } else {
            photo.internalFileName
        }

        val request = ImageRequest.Builder(context)
            .data(
                EncryptedImageRequestData(
                    internalFileName = sourceFileName,
                    mimeType = photo.type.mimeType,
                )
            )
            .size(THUMBNAIL_SIZE)
            .scale(Scale.FIT)
            .allowHardware(false)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .build()

        val result = encryptedImageLoader.execute(request)
        if (result !is SuccessResult) {
            return@withContext Result.failure(Exception("Could not decode $sourceFileName"))
        }

        val tmpFileName = photo.internalThumbnailFileName + TMP_SUFFIX
        val output = vaultFileStorage.openEncryptedOutput(tmpFileName)
            ?: return@withContext Result.failure(Exception("Could not open $tmpFileName"))

        runCatching {
            output.use { out ->
                val bitmap = result.drawable.toBitmap()
                bitmap.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_QUALITY, out)
                bitmap.recycle()
            }
        }.onFailure {
            vaultFileStorage.deleteEncryptedFile(tmpFileName)
            return@withContext Result.failure(it)
        }

        if (!vaultFileStorage.renameEncryptedFile(tmpFileName, photo.internalThumbnailFileName)) {
            vaultFileStorage.deleteEncryptedFile(tmpFileName)
            return@withContext Result.failure(Exception("Could not replace ${photo.internalThumbnailFileName}"))
        }

        photoRepository.markThumbnailUpToDate(photo)
        Result.success(Unit)
    }
}
