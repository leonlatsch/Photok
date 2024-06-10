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
import dev.leonlatsch.photok.model.io.ThumbnailManager.ThumbnailType.PHOTO
import dev.leonlatsch.photok.model.io.ThumbnailManager.ThumbnailType.VIDEO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Manages the creation and loading of thumbnails for photos and videos.
 *
 * @param context the application context
 * @param encryptedStorageManager the [EncryptedStorageManager] to access the encrypted files
 *
 * @since 1.7.2
 * @author Starry Shivam
 */
class ThumbnailManager(
    private val context: Context,
    private val encryptedStorageManager: EncryptedStorageManager
) {
    /**
     * Types of the preview.
     *
     * @property VIDEO for video preview
     * @property PHOTO for photo preview
     */
    enum class ThumbnailType { VIDEO, PHOTO }

    companion object {
        private const val THUMBNAIL_SIZE = 128
    }

    /**
     * Loads the full size thumbnail stored for this photo as a [ByteArray]
     *
     * @param photo the photo to load the thumbnail for
     * @return the thumbnail as a [ByteArray]
     */
    fun loadThumbnail(photo: Photo): ByteArray? {
        encryptedStorageManager.internalOpenEncryptedFileInput(photo.internalThumbnailFileName)
            ?.use { return it.readBytes() }

        return null
    }

    /**
     * Load the full size preview for a stored photo as a [ByteArray]
     *
     * @param photo the photo to load the preview for
     * @return the preview as a [ByteArray]
     */
    fun loadVideoPreview(photo: Photo): ByteArray? {
        encryptedStorageManager.internalOpenEncryptedFileInput(photo.internalVideoPreviewFileName)
            ?.use { return it.readBytes() }

        return null
    }

    /**
     * Create a thumbnail for a photo or video asynchronously.
     *
     * @param photo the photo to create the thumbnail for
     * @param obj the object to create the thumbnail from
     * @param thumbnailType the type of the preview
     */
    suspend fun createThumbnail(photo: Photo, obj: Any?, thumbnailType: ThumbnailType) {
        internalCreateThumbnail(photo, obj, thumbnailType)
    }

    // Internal function to create the thumbnails.
    private suspend fun internalCreateThumbnail(
        photo: Photo,
        obj: Any?,
        thumbnailType: ThumbnailType
    ) =
        withContext(Dispatchers.IO) {
            val imageLoader = ImageLoader.Builder(context)
                // For generating video thumbnails.
                .components { add(VideoFrameDecoder.Factory()) }
                .build()

            val request = ImageRequest.Builder(context)
                .data(obj)
                .size(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
                .scale(Scale.FILL)
                .allowHardware(false)
                .target(
                    onSuccess = { result ->
                        val outputStream = when (thumbnailType) {
                            VIDEO -> encryptedStorageManager.internalOpenEncryptedFileOutput(photo.internalVideoPreviewFileName)
                            PHOTO -> encryptedStorageManager.internalOpenEncryptedFileOutput(photo.internalThumbnailFileName)
                        }
                        outputStream?.use {
                            result.toBitmap().compress(Bitmap.CompressFormat.JPEG, 100, it)
                        }
                    },
                    onError = {
                        Timber.e("Error creating thumbnail for ${photo.fileName}")
                    }
                )
                .build()

            imageLoader.execute(request)
        }

}