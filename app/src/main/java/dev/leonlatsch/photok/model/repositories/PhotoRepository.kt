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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import androidx.paging.PagingSource
import dev.leonlatsch.photok.model.database.dao.PhotoDao
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.security.EncryptionManager
import timber.log.Timber
import java.io.ByteArrayOutputStream
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
    private val encryptionManager: EncryptionManager
) {

    ////////////////////////////// DB //////////////////////////////

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

    ////////////////////////////// IO //////////////////////////////

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
        val success = writePhotoData(context, savedPhoto.uuid, bytes)
        if (!success) {
            val photoToRemove = get(id.toInt())
            delete(photoToRemove)
        }

        return success
    }

    /**
     * Write a [ByteArray] of a photo to the filesystem.
     * Also create a thumbnail for it using [createAndWriteThumbnail].
     *
     * @param context used to write file.
     * @param uuid used for the file name.
     * @param bytes the photo data to save.
     *
     * @return false if any error happened
     *
     * @since 1.0.0
     */
    fun writePhotoData(
        context: Context,
        uuid: String,
        bytes: ByteArray,
        password: String? = null
    ): Boolean {
        return try {
            val encryptedBytes = if (password == null) {
                encryptionManager.encrypt(bytes)
            } else {
                encryptionManager.encrypt(bytes, password)
            }
            context.openFileOutput(Photo.internalFileName(uuid), Context.MODE_PRIVATE).use {
                it.write(encryptedBytes)
            }
            createAndWriteThumbnail(context, uuid, bytes, password)
        } catch (e: IOException) {
            Timber.d("Error writing photo data for id: $uuid $e")
            false
        }
    }

    private fun createAndWriteThumbnail(
        context: Context,
        uuid: String,
        bytes: ByteArray,
        password: String? = null
    ): Boolean {
        return try {
            // Create thumbnail
            val thumbnail = ThumbnailUtils.extractThumbnail(
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size),
                THUMBNAIL_SIZE,
                THUMBNAIL_SIZE
            )
            val outputStream = ByteArrayOutputStream()
            thumbnail.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val thumbnailBytes = outputStream.toByteArray()

            // Write to fs
            val encryptedThumbnailBytes = if (password == null) {
                encryptionManager.encrypt(thumbnailBytes)
            } else {
                encryptionManager.encrypt(thumbnailBytes, password)
            }
            context.openFileOutput(Photo.internalThumbnailFileName(uuid), Context.MODE_PRIVATE)
                .use {
                    it.write(encryptedThumbnailBytes)
                }
            true
        } catch (e: IOException) {
            Timber.d("Error creating Thumbnail for id: $uuid: $e")
            false
        }
    }

    // endregion

    // region READ

    /**
     * Read a photo's bytes from external storage.
     *
     * @param contentResolver Reads the file system
     * @param imageUri The uri to the original file
     */
    fun readPhotoFromExternal(contentResolver: ContentResolver, imageUri: Uri): ByteArray? {
        return try {
            contentResolver.openInputStream(imageUri)?.readBytes()
        } catch (e: IOException) {
            Timber.d("Error opening input stream for uri: $imageUri $e")
            null
        }
    }

    /**
     * Read and decrypt a photo's bytes from internal storage.
     *
     * @param context To open the file
     * @param id The photo's uuid
     */
    suspend fun readPhotoData(context: Context, id: Int): ByteArray? {
        val uuid = getUUID(id)
        return readAndDecryptFile(context, Photo.internalFileName(uuid))
    }

    /**
     * Read a photo's raw bytes.
     * Used similar as [readPhotoData]
     */
    fun readRawPhotoData(context: Context, photo: Photo): ByteArray? =
        readRawBytes(context, Photo.internalFileName(photo.internalFileName))

    /**
     * Read and decrypt a photo's thumbnail from internal storage.
     */
    suspend fun readPhotoThumbnailData(context: Context, id: Int): ByteArray? {
        val uuid = getUUID(id)
        return readAndDecryptFile(context, Photo.internalThumbnailFileName(uuid))
    }

    private fun readAndDecryptFile(context: Context, fileName: String): ByteArray? {
        return try {
            val fileInputStream = context.openFileInput(fileName)
            val encryptedBytes = fileInputStream.readBytes()
            encryptionManager.decrypt(encryptedBytes)
        } catch (e: IOException) {
            Timber.d("Error reading file: $fileName $e")
            null
        }
    }

    private fun readRawBytes(context: Context, fileName: String): ByteArray? {
        return try {
            val fileInputStream = context.openFileInput(fileName)
            val encryptedBytes = fileInputStream.readBytes()
            encryptedBytes
        } catch (e: IOException) {
            Timber.d("Error reading file: $fileName $e")
            null
        }
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

        val success = deletePhotoData(context, id) // Delete bytes on disk
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
    suspend fun deletePhotoData(context: Context, id: Int): Boolean {
        val photo = get(id)
        return (deleteFile(context, photo.internalFileName)
                && deleteFile(context, photo.internalThumbnailFileName))
    }

    private fun deleteFile(context: Context, fileName: String): Boolean {
        val success = context.deleteFile(fileName)
        if (!success) {
            Timber.d("Error deleting file: $fileName")
        }
        return success
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
            val bytes = readPhotoData(context, photo.id!!)
            bytes ?: return false

            val contentValues = ContentValues()
            contentValues.put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "photok_export_${photo.fileName}"
            )
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, photo.type.mimeType)

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            uri ?: return false
            context.contentResolver.openOutputStream(uri).let {
                it?.write(bytes)
            }
            true
        } catch (e: IOException) {
            Timber.d("Error exporting file: ${photo.fileName}")
            false
        }
    }

    // endregion

    companion object {
        private const val THUMBNAIL_SIZE = 128
    }
}