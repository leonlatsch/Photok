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
import coil.size.Size
import coil.transform.Transformation
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.model.database.entity.Photo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.OutputStream
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
    private val encryptedStorageManager: EncryptedStorageManager
) {

    /**
     * @param photo The photo object for which the thumbnail is to be created.
     * @param photoBytes The original full bytes of the photo
     */
    suspend operator fun invoke(photo: Photo, photoBytes: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        val deferredResult = CompletableDeferred<Result<Unit>>()

        val imageLoader = ImageLoader.Builder(context)
            .components { add(VideoFrameDecoder.Factory()) }
            .build()

        val request = ImageRequest.Builder(context)
            .data(photoBytes)
            .size(THUMBNAIL_SIZE)
            .transformations(CenterCropTransformation)
            .allowHardware(false)
            .target(
                onSuccess = { result ->
                    try {
                        encryptedStorageManager.internalOpenEncryptedFileOutput(photo.internalThumbnailFileName)?.use { out ->
                            result.toBitmap().writeTo(out)
                        }

                        if (photo.type.isVideo) {
                            encryptedStorageManager.internalOpenEncryptedFileOutput(photo.internalVideoPreviewFileName)?.use { out ->
                                result.toBitmap().writeTo(out)
                            }
                        }

                        deferredResult.complete(Result.success(Unit))
                    } catch (e: Exception) {
                        deferredResult.complete(Result.failure(e))
                    }
                },
                onError = {
                    val errorMessage = "Error creating thumbnails for ${photo.fileName}"
                    Timber.e(errorMessage)
                    deferredResult.complete(
                        Result.failure(
                            Exception(errorMessage)
                        )
                    )
                }
            )
            .build()

        imageLoader.execute(request)
        deferredResult.await()

    }

    private fun Bitmap.writeTo(out: OutputStream) {
        compress(Bitmap.CompressFormat.JPEG, 100, out)
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