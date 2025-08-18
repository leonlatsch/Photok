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

package dev.leonlatsch.photok.model.io

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import dev.leonlatsch.photok.other.IV_SIZE
import dev.leonlatsch.photok.security.EncryptionManager
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.inject.Inject

/**
 * Manages automatic encrypted internal storage.
 *
 * @since 1.3.0
 * @author Leon Latsch
 */
class EncryptedStorageManager @Inject constructor(
    private val encryptionManager: EncryptionManager,
    private val app: Application
) {

    // region internal

    /**
     * Opens a [CipherInputStream] for an internal file.
     */
    fun internalOpenEncryptedFileInput(
        fileName: String,
        password: String? = null
    ): CipherInputStream? =
        try {
            val inputStream = app.openFileInput(fileName)
            encryptionManager.createCipherInputStream(inputStream, password)
        } catch (e: IOException) {
            Timber.d("Error opening internal file: $fileName: $e")
            null
        }

    /**
     * Opens a [InputStream] for an internal file.
     */
    fun internalOpenFileInput(fileName: String): InputStream? =
        try {
            app.openFileInput(fileName)
        } catch (e: IOException) {
            Timber.d("Error opening internal file: $fileName: $e")
            null
        }

    /**
     * Opens a [CipherOutputStream] for an internal file.
     */
    fun internalOpenEncryptedFileOutput(
        fileName: String,
        password: String? = null
    ): CipherOutputStream? =
        try {
            val outputStream = app.openFileOutput(fileName, INTERNAL_FILE_MODE)
            encryptionManager.createCipherOutputStream(outputStream, password)
        } catch (e: IOException) {
            Timber.d("Error opening internal file: $fileName: $e")
            null
        }

    /**
     * Opens a [OutputStream] for an internal file.
     */
    fun internalOpenFileOutput(fileName: String): OutputStream? =
        try {
            app.openFileOutput(fileName, INTERNAL_FILE_MODE)
        } catch (e: IOException) {
            Timber.d("Error opening internal file: $fileName: $e")
            null
        }

    /**
     * Delete a file in internal storage.
     */
    fun internalDeleteFile(fileName: String): Boolean {
        val success = app.deleteFile(fileName)
        if (!success) {
            Timber.d("Error deleting internal file: $fileName")
        }

        return success
    }

    /**
     * Rename a file in internal storage.
     */
    fun renameFile(currentFileName: String, newFileName: String): Boolean {
        val currentFile = app.getFileStreamPath(currentFileName)
        val newFile = app.getFileStreamPath(newFileName)
        return currentFile.renameTo(newFile)
    }

    /**
     * Re-encrypt a file with a new password.
     */
    fun reEncryptFile(fileName: String, password: String): Boolean {
        val tmpFileName = ".tmp~$fileName"

        val origInput = internalOpenEncryptedFileInput(fileName)
        val tmpOutput = internalOpenEncryptedFileOutput(tmpFileName, password)

        origInput ?: return false
        tmpOutput ?: return false

        val bytesCopied = origInput.copyTo(tmpOutput)
        origInput.close()
        tmpOutput.close()

        var success = bytesCopied > 0

        success = success && internalDeleteFile(fileName)
        success = success && renameFile(tmpFileName, fileName)

        return success
    }

    // endregion

    // region external

    /**
     * Opens a [InputStream] on an external file.
     */
    fun externalOpenFileInput(fileUri: Uri): InputStream? =
        try {
            app.contentResolver.openInputStream(fileUri)
        } catch (e: Exception) {
            Timber.d("Error opening external file at $fileUri: $e")
            null
        }

    /**
     * Opens a [OutputStream] on an external file.
     */
    fun externalOpenFileOutput(
        contentResolver: ContentResolver,
        filename: String,
        mimeType: String,
        destinationUri: Uri
    ): OutputStream? {
        return try {
            DocumentFile.fromTreeUri(app, destinationUri)?.let { dir ->
                val newFile = dir.createFile(mimeType, filename)
                newFile?.uri?.let { contentResolver.openOutputStream(it) }
            }
        } catch (e: IOException) {
            Timber.d("Error opening external file at $destinationUri: $e")
            null
        }
    }

    fun externalDeleteFile(fileUri: Uri): Boolean? =
        try {
            val srcDoc = DocumentFile.fromSingleUri(app.baseContext, fileUri);
            srcDoc?.delete()
        } catch (e: IOException) {
            Timber.d("Error deleting external file at $fileUri: $e")
            null
        }

    // endregion

    companion object {
        private const val INTERNAL_FILE_MODE = Context.MODE_PRIVATE
    }
}