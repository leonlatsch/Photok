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

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import dev.leonlatsch.photok.model.database.dao.PhotoDao
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.model.io.PhotoStorage
import dev.leonlatsch.photok.other.getFileName
import timber.log.Timber
import java.io.IOException
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
    private val photoStorage: PhotoStorage
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
            "image/png" -> PhotoType.PNG
            "image/jpeg" -> PhotoType.JPEG
            "image/gif" -> PhotoType.GIF
            "video/mp4" -> PhotoType.MP4
            else -> return false
        }

        val fileName =
            getFileName(context.contentResolver, externalUri) ?: UUID.randomUUID().toString()

        val origBytes = readPhotoFileFromExternal(context.contentResolver, externalUri)
        origBytes ?: return false

        val photo = Photo(fileName, System.currentTimeMillis(), type, origBytes.size.toLong())

        return safeCreatePhoto(context, photo, origBytes)
    }

    /**
     * Safely insert a photo to the database and write its bytes to the filesystem.
     * Handles IOErrors.
     *
     * @return true, if everything was successfully inserted and written to io.
     */
    suspend fun safeCreatePhoto(context: Context, photo: Photo, bytes: ByteArray): Boolean {
        var success = photoStorage.writePhotoFile(context, photo, bytes)
        if (success) {
            val photoId = insert(photo)
            success = photoId != -1L
        }

        return success
    }

    /**
     * @see PhotoStorage.writePhotoFile
     */
    fun writePhotoFile(
        context: Context,
        photo: Photo,
        bytes: ByteArray,
        password: String? = null
    ): Boolean = photoStorage.writePhotoFile(context, photo, bytes, password)

    // endregion

    // region READ

    /**
     * Read a photo's bytes from external storage.
     *
     * @param contentResolver Reads the file system
     * @param imageUri The uri to the original file
     */
    private fun readPhotoFileFromExternal(
        contentResolver: ContentResolver,
        imageUri: Uri
    ): ByteArray? =
        photoStorage.readFileFromExternal(contentResolver, imageUri)

    /**
     * Read and decrypt a photo's bytes from internal storage.
     *
     * @param context To open the file
     * @param id The photo's uuid
     */
    suspend fun readPhotoFileFromInternal(context: Context, id: Int): ByteArray? {
        val uuid = getUUID(id)
        uuid ?: return null
        return photoStorage.readAndDecryptFile(context, Photo.internalFileName(uuid))
    }

    /**
     * Read a photo's raw bytes.
     * Used similar as [readPhotoFileFromInternal]
     */
    fun readRawPhotoFileFromInternal(context: Context, photo: Photo): ByteArray =
        photoStorage.readRawFile(context, photo.internalFileName)

    /**
     * Read and decrypt a photo's thumbnail from internal storage.
     */
    suspend fun readPhotoThumbnailFromInternal(context: Context, id: Int): ByteArray? {
        val uuid = getUUID(id)
        uuid ?: return null
        return photoStorage.readAndDecryptFile(context, Photo.internalThumbnailFileName(uuid))
    }

    // endregion

    // region DELETE

    /**
     * Delete a photo from the filesystem. On success, delete it in the database.
     *
     * @return true, if the photo was successfully deleted on disk and in db.
     */
    suspend fun safeDeletePhoto(context: Context, photo: Photo): Boolean {
        val uuid = photo.uuid

        val deletedElements = delete(photo)
        val success = deletedElements != -1

        if (success) {
            deletePhotoFiles(context, uuid)
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
    fun deletePhotoFiles(context: Context, uuid: String): Boolean {
        return (photoStorage.deleteFile(context, Photo.internalFileName(uuid))
                && photoStorage.deleteFile(context, Photo.internalThumbnailFileName(uuid)))
    }


    // endregion

    // region EXPORT

    /**
     * Export a photo to a specific directory.
     *
     * @param context To save the file
     * @param photo The Photo to be saved
     */
    suspend fun exportPhoto(context: Context, photo: Photo): Boolean {
        return try {
            val bytes = readPhotoFileFromInternal(context, photo.id!!)
            bytes ?: return false

            val contentValues = ContentValues()
            contentValues.put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "photok_export_${photo.fileName}"
            )
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, photo.type.mimeType)

            photoStorage.insertAndOpenExternalFile(
                context.contentResolver,
                contentValues,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            ) {
                it?.write(bytes)
            }
            true
        } catch (e: IOException) {
            Timber.d("Error exporting file: ${photo.fileName}")
            false
        }
    }

    // endregion
}