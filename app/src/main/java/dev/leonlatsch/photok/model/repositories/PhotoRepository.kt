/*
 *   Copyright 2020 Leon Latsch
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
import androidx.paging.PagingSource
import dev.leonlatsch.photok.model.database.dao.PhotoDao
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.io.PhotoStorage
import timber.log.Timber
import java.io.IOException
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
     * Insert one [Photo]
     *
     * @return the id of the new inserted item.
     */
    private suspend fun insert(photo: Photo) = photoDao.insert(photo)

    /**
     * Delete one [Photo]
     *
     * @return the id of the deleted item.
     */
    private suspend fun delete(photo: Photo) = photoDao.delete(photo)

    /**
     * Delete all photo records.
     */
    suspend fun deleteAll() = photoDao.deleteAll()

    /**
     * Get one [Photo] by [id].
     *
     * @return the photo with [id]
     */
    suspend fun get(id: Int) = photoDao.get(id)

    /**
     * Get all photos, ordered by imported At (desc).
     * Used for re-encrypting.
     *
     * @return all photos as [List]
     */
    suspend fun getAll() = photoDao.getAllSortedByImportedAt()

    /**
     * Get all photos, ordered by importedAt (desc) as [PagingSource].
     * Used for Paging all photos in gallery.
     *
     * @return all photo as [PagingSource]
     */
    fun getAllPaged() = photoDao.getAllPagedSortedByImportedAt()


    /**
     * Get all photo Ids.
     */
    suspend fun getAllIds() = photoDao.getAllIds()

    /**
     * Get uuid for a photo
     */
    private suspend fun getUUID(id: Int) = photoDao.getUUIDForPhoto(id)

    // endregion

    // region IO

    // region WRITE

    /**
     * Safely insert a photo to the database and write its bytes to the filesystem.
     * Handles IOErrors.
     *
     * @return true, if everything was successfully inserted and written to io.
     */
    suspend fun safeCreatePhoto(context: Context, photo: Photo, bytes: ByteArray): Boolean {
        val id = insert(photo)
        val savedPhoto = get(id.toInt())
        val success = photoStorage.writePhotoFile(context, savedPhoto.uuid, bytes)
        if (!success) {
            val photoToRemove = get(id.toInt())
            delete(photoToRemove)
        }

        return success
    }

    fun writePhotoFile(
        context: Context,
        uuid: String,
        bytes: ByteArray,
        password: String? = null
    ): Boolean = photoStorage.writePhotoFile(context, uuid, bytes, password)

    // endregion

    // region READ

    /**
     * Read a photo's bytes from external storage.
     *
     * @param contentResolver Reads the file system
     * @param imageUri The uri to the original file
     */
    fun readPhotoFileFromExternal(contentResolver: ContentResolver, imageUri: Uri): ByteArray? =
        photoStorage.readFileFromExternal(contentResolver, imageUri)

    /**
     * Read and decrypt a photo's bytes from internal storage.
     *
     * @param context To open the file
     * @param id The photo's uuid
     */
    suspend fun readPhotoFileFromInternal(context: Context, id: Int): ByteArray? {
        val uuid = getUUID(id)
        return photoStorage.readAndDecryptFile(context, Photo.internalFileName(uuid))
    }

    /**
     * Read a photo's raw bytes.
     * Used similar as [readPhotoFileFromInternal]
     */
    fun readRawPhotoFileFromInternal(context: Context, photo: Photo): ByteArray? =
        photoStorage.readRawFile(context, photo.internalFileName)

    /**
     * Read and decrypt a photo's thumbnail from internal storage.
     */
    suspend fun readPhotoThumbnailFromInternal(context: Context, id: Int): ByteArray? {
        val uuid = getUUID(id)
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
        val id = photo.id!!

        val success = deletePhotoFiles(context, id) // Delete bytes on disk
        if (success) {
            delete(photo)
        }

        return success
    }

    /**
     * Delete a photos bytes and thumbnail bytes on the filesystem.
     *
     * @param context used for io
     * @param id Id of the photo to delete
     *
     * @return true, if photo and thumbnail could be deleted
     */
    suspend fun deletePhotoFiles(context: Context, id: Int): Boolean {
        val photo = get(id)
        return (photoStorage.deleteFile(context, photo.internalFileName)
                && photoStorage.deleteFile(context, photo.internalThumbnailFileName))
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