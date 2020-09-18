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
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
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
 */
class PhotoRepository @Inject constructor(
    private val photoDao: PhotoDao,
    private val encryptionManager: EncryptionManager
) {

    // DATABASE

    suspend fun insert(photo: Photo) = photoDao.insert(photo)

    suspend fun insertAll(photos: List<Photo>) = photoDao.insertAll(photos)

    suspend fun delete(photo: Photo) = photoDao.delete(photo)

    suspend fun get(id: Int) = photoDao.get(id)

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
        } catch (e: Exception) {
            Timber.d("Error writing photo data for id: $id $e")
            false
        }
    }

    /**
     * Create a thumbnail of a photo's bytes.
     *
     * @param context used to write file.
     * @param id used for the file name.
     * @param bytes the full size photo bytes.
     *
     * * @return false if any error happened
     *
     * @since 1.0.0
     */
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
        } catch (e: Exception) {
            Timber.d("Error creating Thumbnail for id: $id: $e")
            false
        }
    }

    /**
     * Read a photo's bytes from external storage.
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

    companion object {
        private const val THUMBNAIL_SIZE = 128
    }
}