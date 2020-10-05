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

    // DATABASE

    /**
     * Insert one [Photo]
     *
     * @return the id of the new inserted item.
     */
    suspend fun insert(photo: Photo) = photoDao.insert(photo)

    /**
     * Delete one [Photo]
     *
     * @return the id of the deleted item.
     */
    suspend fun delete(photo: Photo) = photoDao.delete(photo)

    /**
     * Get one [Photo] by [id].
     *
     * @return the photo with [id]
     */
    suspend fun get(id: Int) = photoDao.get(id)

    /**
     * Get all photos, ordered by importedAt (desc) as [PagingSource].
     * Used for Paging all photos in gallery.
     *
     * @return all photo as [PagingSource]
     */
    fun getAllPaged() = photoDao.getAllPagedSortedByImportedAt()

    // FILESYSTEM

    /**
     * Write a [ByteArray] of a photo to the filesystem.
     * Also create a thumbnail for it using [createAndWriteThumbnail].
     *
     * @param context used to write file.
     * @param id used for the file name.
     * @param bytes the photo data to save.
     *
     * @return false if any error happened
     *
     * @since 1.0.0
     */
    fun writePhotoData(context: Context, id: Long, bytes: ByteArray): Boolean {
        return try {
            val encryptedBytes = encryptionManager.encrypt(bytes)
            context.openFileOutput("${id}.photok", Context.MODE_PRIVATE).use {
                it.write(encryptedBytes)
            }
            createAndWriteThumbnail(context, id, bytes)
        } catch (e: IOException) {
            Timber.d("Error writing photo data for id: $id $e")
            false
        }
    }

    private fun createAndWriteThumbnail(context: Context, id: Long, bytes: ByteArray): Boolean {
        return try {
            val thumbnail = ThumbnailUtils.extractThumbnail(
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size),
                THUMBNAIL_SIZE,
                THUMBNAIL_SIZE
            )
            val outputStream = ByteArrayOutputStream()
            thumbnail.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val thumbnailBytes = outputStream.toByteArray()
            val encryptedThumbnailBytes = encryptionManager.encrypt(thumbnailBytes)
            context.openFileOutput("${id}.photok.tn", Context.MODE_PRIVATE).use {
                it.write(encryptedThumbnailBytes)
            }
            true
        } catch (e: IOException) {
            Timber.d("Error creating Thumbnail for id: $id: $e")
            false
        }
    }

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
     * @param id The photo's id
     */
    fun readPhotoData(context: Context, id: Int): ByteArray? =
        readAndDecryptFile(context, "${id}.photok")

    /**
     * Read and decrypt a photo's thumbnail from internal storage.
     */
    fun readPhotoThumbnailData(context: Context, id: Int): ByteArray? =
        readAndDecryptFile(context, "${id}.photok.tn")

    private fun readAndDecryptFile(context: Context, fileName: String): ByteArray? {
        return try {
            val fileInputStream = context.openFileInput(fileName)
            val encryptedBytes = fileInputStream.readBytes()
            encryptionManager.decrypt(encryptedBytes)
        } catch (e: IOException) {
            Timber.d(javaClass.toString(), "Error reading file: $fileName $e")
            null
        }
    }

    /**
     * Delete a photo from the filesystem. On success, delete it in the database.
     *
     * @return true, if the photo was successfully deleted on disk and in db.
     */
    suspend fun deletePhotoAndData(context: Context, photo: Photo): Boolean {
        val id = photo.id!!

        val success = deletePhotoData(context, id) // Delete bytes on disk
        if (success) {
            delete(photo)
        }

        return success
    }

    private fun deletePhotoData(context: Context, id: Int): Boolean =
        deleteFile(context, "$id.photok")
                && deleteFile(context, "$id.photok.tn")

    private fun deleteFile(context: Context, fileName: String): Boolean {
        val success = context.deleteFile(fileName)
        if (!success) {
            Timber.d("Error deleting file: $fileName")
        }
        return success
    }

    /**
     * Export a photo to a specific directory.
     *
     * @param context To save the file
     * @param photo The Photo to be saved
     */
    fun exportPhoto(context: Context, photo: Photo): Boolean {
        return try {
            val bytes = readPhotoData(context, photo.id!!)
            bytes ?: return false

            val contentValues = ContentValues()
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, photo.fileName)
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

    companion object {
        private const val THUMBNAIL_SIZE = 128
    }
}