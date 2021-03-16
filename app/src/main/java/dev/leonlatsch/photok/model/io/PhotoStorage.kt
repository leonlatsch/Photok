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
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
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

    // region internal

    /**
     * Opens a [CipherInputStream] for an internal file.
     */
    fun internalOpenEncryptedFileInput(
        context: Context,
        fileName: String,
        password: String? = null
    ): CipherInputStream? =
        try {
            val inputStream = context.openFileInput(fileName)
            encryptionManager.createCipherInputStream(inputStream, password)
        } catch (e: IOException) {
            Timber.d("Error opening internal file: $fileName: $e")
            null
        }

    /**
     * Opens a [InputStream] for an internal file.
     */
    fun internalOpenFileInput(context: Context, fileName: String): InputStream? =
        try {
            context.openFileInput(fileName)
        } catch (e: IOException) {
            Timber.d("Error opening internal file: $fileName: $e")
            null
        }

    /**
     * Opens a [CipherOutputStream] for an internal file.
     */
    fun internalOpenEncryptedFileOutput(
        context: Context,
        fileName: String,
        password: String? = null
    ): CipherOutputStream? =
        try {
            val outputStream = context.openFileOutput(fileName, INTERNAL_FILE_MODE)
            encryptionManager.createCipherOutputStream(outputStream, password)
        } catch (e: IOException) {
            Timber.d("Error opening internal file: $fileName: $e")
            null
        }

    /**
     * Opens a [OutputStream] for an internal file.
     */
    fun internalOpenFileOutput(context: Context, fileName: String): OutputStream? =
        try {
            context.openFileOutput(fileName, INTERNAL_FILE_MODE)
        } catch (e: IOException) {
            Timber.d("Error opening internal file: $fileName: $e")
            null
        }

    /**
     * Delete a file in internal storage.
     */
    fun internalDeleteFile(context: Context, fileName: String): Boolean {
        val success = context.deleteFile(fileName)
        if (!success) {
            Timber.d("Error deleting internal file: $fileName")
        }

        return success
    }

    private fun createThumbnail(
        context: Context,
        photo: Photo,
        extFileUri: Uri,
        password: String? = null
    ) {
        val thumbnail = Glide.with(context)
            .asBitmap()
            .load(extFileUri)
            .centerCrop()
            .submit(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
            .get()

        internalOpenEncryptedFileOutput(context, photo.internalThumbnailFileName, password).use {
            thumbnail.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    // endregion

    // region external

    /**
     * Opens a [InputStream] on an external file.
     */
    fun externalOpenFileInput(contentResolver: ContentResolver, fileUri: Uri): InputStream? =
        try {
            contentResolver.openInputStream(fileUri)
        } catch (e: IOException) {
            Timber.d("Error opening external file at $fileUri: $e")
            null
        }

    /**
     * Opens a [OutputStream] on an external file.
     */
    fun externalOpenFileOutput(
        contentResolver: ContentResolver,
        contentValues: ContentValues,
        destinationUri: Uri
    ): OutputStream? {
        return try {
            val externalUrl = contentResolver.insert(destinationUri, contentValues) ?: return null
            contentResolver.openOutputStream(externalUrl)
        } catch (e: IOException) {
            Timber.d("Error opening external file at $destinationUri: $e")
            null
        }
    }

    // endregion

    /**
     * Write a [InputStream] to a [OutputStream] with a buffer.
     *
     * @return true, if stream was successfully copied
     */
    fun writeBuffered(inputStream: InputStream, outputStream: OutputStream): Boolean {
        val buffer = ByteArray(BUFFER_SIZE)
        var len: Int

        return try {
            len = inputStream.read(buffer)
            while (len != -1) {
                outputStream.write(buffer, 0, len)
                len = inputStream.read(buffer)
            }

            true
        } catch (e: IOException) {
            Timber.d("Error copying streams: $e")
            false
        }
    }

    companion object {
        private const val THUMBNAIL_SIZE = 128
        private const val BUFFER_SIZE = 1024
        private const val INTERNAL_FILE_MODE = Context.MODE_PRIVATE
    }
}