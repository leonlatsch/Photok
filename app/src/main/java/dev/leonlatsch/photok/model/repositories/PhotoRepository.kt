/*
 *   Copyright 2020-2022 Leon Latsch
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
import android.content.Intent
import android.net.Uri
import dev.leonlatsch.photok.model.database.dao.AlbumDao
import dev.leonlatsch.photok.model.database.dao.PhotoDao
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.model.io.CreateThumbnailsUseCase
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import dev.leonlatsch.photok.other.extensions.empty
import dev.leonlatsch.photok.other.extensions.lazyClose
import dev.leonlatsch.photok.other.getFileName
import dev.leonlatsch.photok.settings.data.Config
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
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
    private val albumDao: AlbumDao,
    private val encryptedStorageManager: EncryptedStorageManager,
    private val createThumbnail: CreateThumbnailsUseCase,
    private val app: Application,
    private val config: Config,
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

    suspend fun get(uuid: String) = photoDao.get(uuid)

    /**
     * @see PhotoDao.getAll
     */
    suspend fun getAll() = photoDao.getAll()

    fun observeAll() = photoDao.observeAll()

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
     * Returns re created uuid
     */
    suspend fun safeImportPhoto(sourceUri: Uri, importSource: ImportSource): String {
        val type = when (app.contentResolver.getType(sourceUri)) {
            PhotoType.PNG.mimeType -> PhotoType.PNG
            PhotoType.JPEG.mimeType -> PhotoType.JPEG
            PhotoType.GIF.mimeType -> PhotoType.GIF
            PhotoType.MP4.mimeType -> PhotoType.MP4
            PhotoType.MPEG.mimeType -> PhotoType.MPEG
            PhotoType.WEBM.mimeType -> PhotoType.WEBM
            else -> return String.empty
        }

        val fileName =
            getFileName(app.contentResolver, sourceUri) ?: UUID.randomUUID().toString()

        val inputStream =
            encryptedStorageManager.externalOpenFileInput(sourceUri)
        val photo = Photo(fileName, System.currentTimeMillis(), type)

        val created = safeCreatePhoto(photo, inputStream, sourceUri)
        inputStream?.lazyClose()

        if (!created) {
            return String.empty
        }

        if (config.deleteImportedFiles && importSource != ImportSource.Share) {
            val deleted = encryptedStorageManager.externalDeleteFile(sourceUri)
            return if (deleted == true) photo.uuid else String.empty
        }

        return photo.uuid
    }

    /**
     * Writes and encrypts the [source] into internal storage.
     * Saves the [photo] afterwords.
     * It is up to the caller to close the [source].
     * Does create a thumbnail, IF [origUri] is specified.
     *
     * @return true, if everything worked
     */
    private suspend fun safeCreatePhoto(
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
            albumDao.unlink(photo.uuid)
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
    suspend fun exportPhoto(photo: Photo, target: Uri): Boolean {
        return try {
            val inputStream =
                encryptedStorageManager.internalOpenEncryptedFileInput(photo.internalFileName)
            inputStream ?: return false

            val outputStream = createExternalOutputStream(photo, target)
            outputStream ?: return false

            val wrote = inputStream.copyTo(outputStream)
            outputStream.lazyClose()

            var deleted = true
            if (config.deleteExportedFiles) {
                deleted = safeDeletePhoto(photo)
            }

            wrote != -1L && deleted
        } catch (e: IOException) {
            Timber.d("Error exporting file: ${photo.fileName}")
            false
        }
    }

    private fun createExternalOutputStream(photo: Photo, uri: Uri): OutputStream? {
        val fileName = "photok_export_${photo.fileName}"
        val mimeType = photo.type.mimeType

        return encryptedStorageManager.externalOpenFileOutput(
            app.contentResolver,
            fileName,
            mimeType,
            uri,
        )
    }

    // endregion
    // endregion
}