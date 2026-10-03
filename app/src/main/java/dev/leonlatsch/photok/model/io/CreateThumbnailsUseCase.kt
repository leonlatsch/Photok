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
import coil.request.ImageRequest
import coil.request.videoFramePercent
import coil.size.Scale
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.io.VaultFileStorage
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.transcoding.domain.ImageStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


/**
 * Maximum size of the longest side of the thumbnail in pixels.
 *
 * Thumbnails keep the original aspect ratio, so the image viewer can show them as preview while the full image loads.
 */
const val THUMBNAIL_SIZE = 1080

const val THUMBNAIL_QUALITY = 80

/**
 * Use case to create all thumbnails for a photo or video.
 *
 * @since 1.7.2
 * @author Starry Shivam
 */
class CreateThumbnailsUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageStorage: ImageStorage,
    private val vaultFileStorage: VaultFileStorage,
) {

    /**
     * @param photo The photo object for which the thumbnail is to be created.
     * @param data The data for the photo. May be ByteArray or system Uri
     */
    suspend operator fun invoke(photo: Photo, data: Any?): Result<Unit> =
        withContext(Dispatchers.IO) {

            // Thumbnail
            val thumbnailRequest = ImageRequest.Builder(context)
                .data(data)
                .size(THUMBNAIL_SIZE)
                .scale(Scale.FIT)
                .allowHardware(false)
                .apply { if (photo.type.isVideo) videoFramePercent(0.5) }
                .build()

            val thumbnailResult = imageStorage.execAndWrite(
                imageRequest = thumbnailRequest,
                outputStream = vaultFileStorage.openEncryptedOutput(photo.internalThumbnailFileName),
                compressionPercent = THUMBNAIL_QUALITY,
            )

            // Video Preview
            val videoPreviewResult = if (photo.type.isVideo) {
                val videoPreviewRequest = ImageRequest.Builder(context)
                    .data(data)
                    .allowHardware(false)
                    .videoFramePercent(0.5)
                    .build()

                imageStorage.execAndWrite(
                    imageRequest = videoPreviewRequest,
                    outputStream = vaultFileStorage.openEncryptedOutput(photo.internalVideoPreviewFileName),
                    compressionPercent = 90,
                )
            } else {
                // Success if not a video
                Result.success(Unit)
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
