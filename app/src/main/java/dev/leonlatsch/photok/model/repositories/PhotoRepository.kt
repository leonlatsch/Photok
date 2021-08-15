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

import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import com.bumptech.glide.Glide
import dev.leonlatsch.photok.model.database.dao.CollectionDao
import dev.leonlatsch.photok.model.database.dao.PhotoDao
import dev.leonlatsch.photok.model.database.entity.Collection
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import dev.leonlatsch.photok.other.extensions.lazyClose
import dev.leonlatsch.photok.other.getFileName
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
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
    private val collectionDao: CollectionDao,
    private val encryptedStorageManager: EncryptedStorageManager,
    private val app: Application
) {

    // region DATABASE

    /**
     * @see PhotoDao.insert
     */
    suspend fun insert(photo: Photo) = photoDao.insert(photo)

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
     * @see PhotoDao.countAll
     */
    suspend fun countAll() = photoDao.countAll()

    // endregion

    // region IO

    // region WRITE

    /**
     * Import a photo from a url.
     *
     * Collects meta data and calls [safeCreatePhoto].
     */
    suspend fun safeImportPhoto(sourceUri: Uri): Boolean {
        val collectionWithPhotos = collectionDao.getCollectionWithPhotos(1)
        Timber.d(collectionWithPhotos.toString())
        val type = when (app.contentResolver.getType(sourceUri)) {
            PhotoType.PNG.mimeType -> PhotoType.PNG
            PhotoType.JPEG.mimeType -> PhotoType.JPEG
            PhotoType.GIF.mimeType -> PhotoType.GIF
            PhotoType.MP4.mimeType -> PhotoType.MP4
            PhotoType.MPEG.mimeType -> PhotoType.MPEG
            else -> return false
        }

        val fileName =
            getFileName(app.contentResolver, sourceUri) ?: UUID.randomUUID().toString()

        val inputStream =
            encryptedStorageManager.externalOpenFileInput(sourceUri)
        val photo = Photo(fileName, System.currentTimeMillis(), type, collectionId = 1)

        val created = safeCreatePhoto(photo, inputStream, sourceUri)
        inputStream?.lazyClose()
        return created
    }

    /**
     * Writes and encrypts the [source] into internal storage.
     * Saves the [photo] afterwords.
     * It is up to the caller to close the [source].
     * Does create a thumbnail, IF [origUri] is specified.
     *
     * @return true, if everything worked
     */
    suspend fun safeCreatePhoto(
        photo: Photo,
        source: InputStream?,
        origUri: Uri? = null
    ): Boolean {
        val fileLen = createPhotoFile(photo, source)
        var success = fileLen != -1L

        if (success) {
            photo.size = fileLen

            if (origUri != null) {
                createThumbnail(photo, origUri)
                if (photo.type.isVideo) {
                    createVideoPreview(photo, origUri)
                }
            }

            val photoId = insert(photo)
            success = photoId != -1L
        }

        return success
    }

    /**
     * Create the internal file for a photo.
     */
    fun createPhotoFile(photo: Photo, source: InputStream?): Long {
        val encryptedDestination =
            encryptedStorageManager.internalOpenEncryptedFileOutput(photo.internalFileName)

        source ?: return -1L
        encryptedDestination ?: return -1L

        val fileLen = source.copyTo(encryptedDestination)
        encryptedDestination.lazyClose()

        return fileLen
    }

    private fun createThumbnail(photo: Photo, sourceUri: Uri) =
        internalCreateThumbnail(photo, sourceUri)

    /**
     * Create a thumbnail from raw bytes.
     */
    fun createThumbnail(photo: Photo, bytes: ByteArray) =
        internalCreateThumbnail(photo, bytes)

    private fun internalCreateThumbnail(photo: Photo, obj: Any?) {
        val thumbnail = Glide.with(app)
            .asBitmap()
            .load(obj)
            .centerCrop()
            .submit(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
            .get()

        encryptedStorageManager.internalOpenEncryptedFileOutput(
            photo.internalThumbnailFileName
        ).use {
            thumbnail?.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
    }

    private fun createVideoPreview(photo: Photo, sourceUri: Uri) {
        val preview = Glide.with(app)
            .asBitmap()
            .load(sourceUri)
            .submit()
            .get()

        encryptedStorageManager.internalOpenEncryptedFileOutput(
            photo.internalVideoPreviewFileName
        ).use {
            preview?.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
    }

    // endregion

    // region READ

    /**
     * Loads the full size file stored for this photo as a [ByteArray].
     * Use with caution!
     */
    fun loadPhoto(photo: Photo): ByteArray? {
        encryptedStorageManager.internalOpenEncryptedFileInput(photo.internalFileName)?.use {
            return it.readBytes()
        }

        return null
    }

    /**
     * Loads the full size thumbnail stored for this photo as a [ByteArray]
     */
    fun loadThumbnail(photo: Photo): ByteArray? {
        encryptedStorageManager.internalOpenEncryptedFileInput(photo.internalThumbnailFileName)
            ?.use {
                return it.readBytes()
            }

        return null
    }

    /**
     * Load the full size preview for a stored photo as a [ByteArray]
     */
    fun loadVideoPreview(photo: Photo): ByteArray? {
        encryptedStorageManager.internalOpenEncryptedFileInput(photo.internalVideoPreviewFileName)
            ?.use {
                return it.readBytes()
            }

        return null
    }

    // endregion

    // region DELETE

    /**
     * Delete a photo from the filesystem. On success, delete it in the database.
     *
     * @return true, if the photo was successfully deleted on disk and in db.
     */
    suspend fun safeDeletePhoto(photo: Photo): Boolean {
        val deletedElements = delete(photo)
        val success = deletedElements != -1

        if (success) {
            deleteInternalPhotoData(photo)
        }

        return success
    }

    /**
     * Delete a photos bytes and thumbnail bytes on the filesystem.
     *
     * @param photo the photo to delete
     *
     * @return true, if photo and thumbnail could be deleted
     */
    fun deleteInternalPhotoData(photo: Photo): Boolean =
        encryptedStorageManager.internalDeleteFile(photo.internalFileName)
                && encryptedStorageManager.internalDeleteFile(photo.internalThumbnailFileName)
                && (!photo.type.isVideo || encryptedStorageManager.internalDeleteFile(photo.internalVideoPreviewFileName))


    // endregion

    // region EXPORT

    /**
     * Export a photo to a specific directory.
     *
     * @param photo The Photo to be saved
     */
    fun exportPhoto(photo: Photo): Boolean {
        return try {
            val inputStream =
                encryptedStorageManager.internalOpenEncryptedFileInput(photo.internalFileName)
            inputStream ?: return false

            val outputStream = createExternalOutputStream(photo)
            outputStream ?: return false

            val wrote = inputStream.copyTo(outputStream)
            outputStream.lazyClose()

            wrote != -1L
        } catch (e: IOException) {
            Timber.d("Error exporting file: ${photo.fileName}")
            false
        }
    }

    private fun createExternalOutputStream(photo: Photo): OutputStream? {
        val mediaColName: String
        val mediaColMimeType: String
        val externalUri: Uri

        if (photo.type.isVideo) {
            mediaColName = MediaStore.Video.Media.DISPLAY_NAME
            mediaColMimeType = MediaStore.Video.Media.MIME_TYPE
            externalUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else {
            mediaColName = MediaStore.Images.Media.DISPLAY_NAME
            mediaColMimeType = MediaStore.Images.Media.MIME_TYPE
            externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(mediaColName, "photok_export_${photo.fileName}")
            put(mediaColMimeType, photo.type.mimeType)
        }

        return encryptedStorageManager.externalOpenFileOutput(
            app.contentResolver,
            contentValues,
            externalUri
        )
    }

    // endregion
    // endregion

    companion object {
        private const val THUMBNAIL_SIZE = 128
    }
}