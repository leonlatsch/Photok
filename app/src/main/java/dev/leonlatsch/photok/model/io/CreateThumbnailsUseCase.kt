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
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.io.VaultFileStorage
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.transcoding.domain.ImageStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

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
    private val thumbnailGenerator: ThumbnailGenerator,
) {

    /**
     * @param photo The photo object for which the thumbnail is to be created.
     * @param data The data for the photo. May be ByteArray or system Uri
     */
    suspend operator fun invoke(photo: Photo, data: Any?): Result<Unit> =
        withContext(Dispatchers.IO) {

            val thumbnailResult = thumbnailGenerator.createFromImport(
                uuid = photo.uuid,
                data = data,
                isVideo = photo.type.isVideo,
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
