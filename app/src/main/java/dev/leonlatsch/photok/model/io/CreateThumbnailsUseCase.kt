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

package dev.leonlatsch.photok.model.io

import android.content.Context
import android.graphics.Bitmap
import coil.request.ImageRequest
import coil.size.Size
import coil.transform.Transformation
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.imageloading.domain.ImageStorage
import dev.leonlatsch.photok.model.database.entity.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


/** Maximum size of the thumbnail in pixels */
private const val THUMBNAIL_SIZE = 256

/**
 * Use case to create all thumbnails for a photo or video.
 *
 * @since 1.7.2
 * @author Starry Shivam
 */
class CreateThumbnailsUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageStorage: ImageStorage,
    private val encryptedStorageManager: EncryptedStorageManager,
) {

    /**
     * @param photo The photo object for which the thumbnail is to be created.
     * @param data The data for the photo. May be ByteArray or system Uri
     */
    suspend operator fun invoke(photo: Photo, data: Any?): Result<Unit> =
        withContext(Dispatchers.IO) {
            val thumbnailRequest = ImageRequest.Builder(context)
                .data(data)
                .size(THUMBNAIL_SIZE)
                .transformations(CenterCropTransformation)
                .allowHardware(false)
                .build()

            val videoPreviewRequest = if (photo.type.isVideo) {
                ImageRequest.Builder(context)
                    .data(data)
                    .allowHardware(false)
                    .build()
            } else {
                null
            }

            val thumbnailResult = imageStorage.execAndWrite(
                imageRequest = thumbnailRequest,
                outputStream = encryptedStorageManager.internalOpenEncryptedFileOutput(photo.internalThumbnailFileName),
            )


            var videoPreviewResult = Result.success(Unit)
            if (videoPreviewRequest != null) {
                videoPreviewResult = imageStorage.execAndWrite(
                    imageRequest = videoPreviewRequest,
                    outputStream = encryptedStorageManager.internalOpenEncryptedFileOutput(photo.internalVideoPreviewFileName),
                )
            }

            if (thumbnailResult.isSuccess && videoPreviewResult.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(
                    thumbnailResult.exceptionOrNull() ?: Exception("error creating thumbnail")
                )
            }
        }
}

object CenterCropTransformation : Transformation {

    override val cacheKey: String = javaClass.name

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val minDim = minOf(input.width, input.height)
        val startX = (input.width - minDim) / 2
        val startY = (input.height - minDim) / 2

        return Bitmap.createBitmap(input, startX, startY, minDim, minDim)
    }
}