/*
 *   Copyright 2020-2021 Leon Latsch
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

package dev.leonlatsch.photok.model.repositories

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import com.bumptech.glide.Glide
import dev.leonlatsch.photok.model.database.dao.PhotoDao
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import dev.leonlatsch.photok.other.getFileName
import dev.leonlatsch.photok.other.lazyClose
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.util.*
import javax.inject.Inject

/**
 * Repository for [Photo].
 * Uses [PhotoDao] and accesses the filesystem to read and write encrypted photos.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class PhotoRepository @Inject constructor(
    private val photoDao: PhotoDao,
    private val encryptedStorageManager: EncryptedStorageManager
) {

    // region DATABASE

    /**
     * @see PhotoDao.insert
     */
    private suspend fun insert(photo: Photo) = photoDao.insert(photo)

    /**
     * @see PhotoDao.delete
     */
    private suspend fun delete(photo: Photo) = photoDao.delete(photo)

    /**
     * @see PhotoDao.deleteAll
     */
    suspend fun deleteAll() = photoDao.deleteAll()

    /**
     * @see PhotoDao.get
     */
    suspend fun get(id: Int) = photoDao.get(id)

    /**
     * @see PhotoDao.getAllSortedByImportedAt
     */
    suspend fun getAll() = photoDao.getAllSortedByImportedAt()

    /**
     * @see PhotoDao.getAllPagedSortedByImportedAt
     */
    fun getAllPaged() = photoDao.getAllPagedSortedByImportedAt()


    /**
     * @see PhotoDao.getAllIds
     */
    suspend fun getAllIds() = photoDao.getAllIds()

    /**
     * @see PhotoDao.getAllUUIDs
     */
    suspend fun getAllUUIDs() = photoDao.getAllUUIDs()

    /**
     * @see PhotoDao.getUUIDForPhoto
     */
    private suspend fun getUUID(id: Int) = photoDao.getUUIDForPhoto(id)

    // endregion

    // region IO

    // region WRITE

    /**
     * Import a photo from a url.
     *
     * Collects meta data and calls [safeCreatePhoto].
     */
    suspend fun safeImportPhoto(context: Context, externalUri: Uri): Boolean {
        val type = when (context.contentResolver.getType(externalUri)) {
            PhotoType.PNG.mimeType -> PhotoType.PNG
            PhotoType.JPEG.mimeType -> PhotoType.JPEG
            PhotoType.GIF.mimeType -> PhotoType.GIF
            PhotoType.MP4.mimeType -> PhotoType.MP4
            PhotoType.MPEG.mimeType -> PhotoType.MPEG
            else -> return false
        }

        val fileName =
            getFileName(context.contentResolver, externalUri) ?: UUID.randomUUID().toString()

        val inputStream =
            encryptedStorageManager.externalOpenFileInput(context.contentResolver, externalUri)
        val photo = Photo(fileName, System.currentTimeMillis(), type)

        val created = safeCreatePhoto(context, photo, inputStream)
        inputStream?.lazyClose()
        return created
    }

    /**
     * Writes and encrypts the [source] into internal storage.
     * Saves the [photo] afterwords.
     * It is up to the caller to close the [source].
     *
     * @return true, if everything worked
     */
    suspend fun safeCreatePhoto(
        context: Context,
        photo: Photo,
        source: InputStream?
    ): Boolean {
        val encryptedDestination =
            encryptedStorageManager.internalOpenEncryptedFileOutput(context, photo.internalFileName)

        source ?: return false
        encryptedDestination ?: return false

        val fileLen = source.copyTo(encryptedDestination)
        var success = fileLen != -1L

        if (success) {
            photo.size = fileLen

            createThumbnail(context, photo, source)

            val photoId = insert(photo)
            success = photoId != -1L
        }

        encryptedDestination.lazyClose()

        return success
    }

    private fun createThumbnail(
        context: Context,
        photo: Photo,
        fullSizeInputStream: InputStream
    ) {
        val thumbnail = Glide.with(context)
            .asBitmap()
            .load(fullSizeInputStream)
            .centerCrop()
            .submit(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
            .get()

        encryptedStorageManager.internalOpenEncryptedFileOutput(
            context,
            photo.internalThumbnailFileName
        ).use {
            thumbnail.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    // endregion

    // region READ

    /**
     * Loads the full size file stored for this photo as a [ByteArray].
     * Use with caution!
     */
    fun loadPhoto(context: Context, photo: Photo): ByteArray? {
        sync(context, photo)

        val bytes = photo.stream?.readBytes()

        deSync(photo)
        return bytes
    }

    /**
     * Loads the full size thumbnail stored for this photo as a [ByteArray]
     */
    fun loadThumbnail(context: Context, photo: Photo): ByteArray? {
        syncThumbnail(context, photo)

        val bytes = photo.thumbnailStream?.readBytes()

        deSyncThumbnail(photo)
        return bytes
    }

    // endregion

    // region DELETE

    /**
     * Delete a photo from the filesystem. On success, delete it in the database.
     *
     * @return true, if the photo was successfully deleted on disk and in db.
     */
    suspend fun safeDeletePhoto(context: Context, photo: Photo): Boolean {
        val deletedElements = delete(photo)
        val success = deletedElements != -1

        if (success) {
            deleteInternalPhotoData(context, photo.uuid)
        }

        return success
    }

    /**
     * Delete a photos bytes and thumbnail bytes on the filesystem.
     *
     * @param context used for io
     * @param uuid UUID of the photo to delete
     *
     * @return true, if photo and thumbnail could be deleted
     */
    fun deleteInternalPhotoData(context: Context, uuid: String): Boolean =
        (encryptedStorageManager.internalDeleteFile(
            context,
            Photo.internalFileName(uuid)
        ) && encryptedStorageManager.internalDeleteFile(
            context,
            Photo.internalThumbnailFileName(uuid)
        ))

    fun deleteTempPhotoCaches(context: Context, photo: Photo): Boolean =
        (encryptedStorageManager.internalDeleteFile(
            context,
            photo.internalCachedFileName
        ) && encryptedStorageManager.internalDeleteFile(
            context,
            photo.internalCachedThumbnailFileName
        ))


    // endregion

    // region EXPORT

    /**
     * Export a photo to a specific directory.
     *
     * @param context To save the file
     * @param photo The Photo to be saved
     */
    fun exportPhoto(context: Context, photo: Photo): Boolean {
        return try {
            val inputStream =
                encryptedStorageManager.internalOpenEncryptedFileInput(
                    context,
                    photo.internalFileName
                )
            inputStream ?: return false

            val contentValues = ContentValues()
            contentValues.put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "photok_export_${photo.fileName}"
            )
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, photo.type.mimeType)

            val outputStream = encryptedStorageManager.externalOpenFileOutput(
                context.contentResolver,
                contentValues,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            outputStream ?: return false

            val wrote = inputStream.copyTo(outputStream)

            wrote != -1L
        } catch (e: IOException) {
            Timber.d("Error exporting file: ${photo.fileName}")
            false
        }
    }

    // endregion

    /**
     * Opens the inputStream for a photo.
     */
    fun sync(context: Context, photo: Photo, password: String? = null) {
        val fullSizeInput = encryptedStorageManager.internalOpenEncryptedFileInput(
            context,
            photo.internalFileName,
            password
        ) ?: return

        photo.stream = fullSizeInput

    }

    /**
     * Closes and deletes the inputStream for a photo.
     */
    fun deSync(photo: Photo) {
        photo.stream?.close()
        photo.stream = null
    }

    /**
     * Opens the inputStream for a photo thumbnail.
     */
    fun syncThumbnail(context: Context, photo: Photo, password: String? = null) {
        val thumbnailInput = encryptedStorageManager.internalOpenEncryptedFileInput(
            context,
            photo.internalThumbnailFileName,
            password
        ) ?: return

        photo.thumbnailStream = thumbnailInput
    }

    /**
     * Closes and deletes the inputStream for a photo thumbnail.
     */
    fun deSyncThumbnail(photo: Photo) {
        photo.thumbnailStream?.close()
        photo.thumbnailStream = null
    }

    // endregion

    companion object {
        private const val THUMBNAIL_SIZE = 128
    }
}