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

package dev.leonlatsch.photok.model.io

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.bumptech.glide.Glide
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.security.EncryptionManager
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import javax.inject.Inject

/**
 * Storage Manager for photos in internal and external storage.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class PhotoStorage @Inject constructor(
    private val encryptionManager: EncryptionManager
) {

    /**
     * Delete a file in internal storage.
     */
    fun deleteFile(context: Context, fileName: String): Boolean {
        val success = context.deleteFile(fileName)
        if (!success) {
            Timber.d("Error deleting file: $fileName")
        }
        return success
    }

    /**
     * Read a file from internal storage.
     */
    fun readRawFile(context: Context, fileName: String): ByteArray {
        return try {
            val fileInputStream = context.openFileInput(fileName)
            val encryptedBytes = fileInputStream.readBytes()
            encryptedBytes
        } catch (e: IOException) {
            Timber.d("Error reading file: $fileName $e")
            throw e
        }
    }

    /**
     * Read and decrypt a file from internal storage using [readRawFile].
     * Used [EncryptionManager] for decryption.
     */
    fun readAndDecryptFile(context: Context, fileName: String): ByteArray? {
        return try {
            val encryptedBytes = readRawFile(context, fileName)
            encryptionManager.decrypt(encryptedBytes)
        } catch (e: IOException) {
            Timber.d("Error reading file: $fileName $e")
            null
        }
    }

    /**
     * Insert a photo to an external content uri and pass the output stream to the [operation].
     * @see insertAndOpenInternalFile
     */
    fun insertAndOpenExternalFile(
        contentResolver: ContentResolver,
        contentValues: ContentValues,
        destination: Uri,
        operation: (outputStream: OutputStream?) -> Unit
    ) {
        val uri = contentResolver.insert(destination, contentValues)
        uri ?: return
        return contentResolver.openOutputStream(uri).use(operation)
    }

    /**
     * Insert a file to internal storage and pass the output stream to the [operation].
     * @see insertAndOpenExternalFile
     */
    fun insertAndOpenInternalFile(
        context: Context,
        fileName: String,
        operation: (outputStream: OutputStream?) -> Unit
    ) {
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use(operation)
    }

    /**
     * Read a file from external storage.
     */
    fun readFileFromExternal(contentResolver: ContentResolver, fileUri: Uri): ByteArray? {
        return try {
            contentResolver.openInputStream(fileUri)?.readBytes()
        } catch (e: IOException) {
            Timber.d("Error opening input stream for uri: $fileUri $e")
            null
        }
    }

    /**
     * Write a [ByteArray] of a photo to the filesystem.
     * Also create a thumbnail for it using [PhotoStorage.createThumbnail].
     *
     * @param context used to write file.
     * @param uuid used for the file name.
     * @param bytes the photo data to save.
     *
     * @return false if any error happened
     *
     * @since 1.0.0
     */
    fun writePhotoFile(
        context: Context,
        photo: Photo,
        bytes: ByteArray,
        password: String? = null
    ): Boolean {
        return try {
            val encryptedBytes = dynamicEncryptBytes(bytes, password)
            encryptedBytes ?: return false

            insertAndOpenInternalFile(context, photo.internalFileName) {
                it?.write(encryptedBytes)
            }

            createThumbnail(context, photo, bytes, password)
        } catch (e: IOException) {
            Timber.d("Error writing photo data for id: ${photo.uuid} $e")
            false
        }
    }

    private fun createThumbnail(
        context: Context,
        photo: Photo,
        origBytes: ByteArray,
        password: String? = null
    ): Boolean {
        return try {
            val thumbnail = Glide.with(context)
                .load(origBytes)
                .asBitmap()
                .centerCrop()
                .thumbnail(THUMBNAIL_SIZE_MULTIPLIER)
                .into(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
                .get()

            val outputStream = ByteArrayOutputStream()
            thumbnail.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val thumbnailBytes = outputStream.toByteArray()

            val encryptedBytes = dynamicEncryptBytes(thumbnailBytes, password)
            encryptedBytes ?: return false

            insertAndOpenInternalFile(context, photo.internalThumbnailFileName) {
                it?.write(encryptedBytes)
            }
            true
        } catch (e: IOException) {
            Timber.d("Error creating Thumbnail for id: ${photo.uuid}: $e")
            false
        }
    }

    private fun dynamicEncryptBytes(bytes: ByteArray, password: String?): ByteArray? {
        return if (password == null) {
            encryptionManager.encrypt(bytes)
        } else {
            encryptionManager.encrypt(bytes, password)
        }
    }

    companion object {
        private const val THUMBNAIL_SIZE = 128
        private const val THUMBNAIL_SIZE_MULTIPLIER = 1f
    }
}