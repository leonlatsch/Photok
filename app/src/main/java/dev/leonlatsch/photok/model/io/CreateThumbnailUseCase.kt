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
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.size.Scale
import dev.leonlatsch.photok.model.database.entity.Photo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber


/** Maximum size of the thumbnail in pixels */
private const val THUMBNAIL_SIZE = 128

/**
 * Use case to create a thumbnail for a photo or video.
 *
 * @param context the application context
 * @param encryptedStorageManager the [EncryptedStorageManager] to access the encrypted files
 *
 * @since 1.7.2
 * @author Starry Shivam
 */
class CreateThumbnailUseCase(
    private val context: Context,
    private val encryptedStorageManager: EncryptedStorageManager
) {

    // invoke operator to create thumbnail
    suspend operator fun invoke(photo: Photo, obj: Any?): Result<Unit> {
        return createThumbnail(photo, obj)
    }

    /**
     * Creates a thumbnail for the given photo. If the photo is a video,
     * it also creates a video preview.
     *
     * @param photo The photo object for which the thumbnail is to be created.
     * @param obj The data for the image request. This could be a URL, file, or any other supported data type.
     * @return A [Result] indicating the success or failure of the thumbnail creation.
     */
    private suspend fun createThumbnail(
        photo: Photo, obj: Any?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val deferredResult = CompletableDeferred<Result<Unit>>()

        val imageLoader = ImageLoader.Builder(context)
            .components { add(VideoFrameDecoder.Factory()) }
            .build()

        val request = ImageRequest.Builder(context)
            .data(obj)
            .size(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
            .scale(Scale.FILL)
            .allowHardware(false)
            .target(
                onSuccess = { result ->
                    try {
                        // Lambda to compress & save bitmap and avoid code duplication.
                        val saveCompressedBitmap: (fileName: String) -> Unit = { fileName ->
                            encryptedStorageManager.internalOpenEncryptedFileOutput(fileName)
                                ?.use { ops ->
                                    result.toBitmap().compress(Bitmap.CompressFormat.JPEG, 100, ops)
                                }
                        }
                        // Create thumbnail
                        saveCompressedBitmap(photo.internalThumbnailFileName)
                        // If the photo is a video, create a video preview
                        if (photo.type.isVideo) {
                            saveCompressedBitmap(photo.internalVideoPreviewFileName)
                        }
                        deferredResult.complete(Result.success(Unit))
                    } catch (e: Exception) {
                        deferredResult.complete(Result.failure(e))
                    }
                },
                onError = {
                    Timber.e("Error creating thumbnail for ${photo.fileName}")
                    deferredResult.complete(
                        Result.failure(Exception("Error creating thumbnail for ${photo.fileName}"))
                    )
                }
            )
            .build()

        imageLoader.execute(request)
        deferredResult.await()
    }


}