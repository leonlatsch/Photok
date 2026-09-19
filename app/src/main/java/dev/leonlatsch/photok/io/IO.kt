/*
 *   Copyright 2020-2026 Leon Latsch
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

package dev.leonlatsch.photok.io

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val COPY_BUFFER_SIZE = 8192

@Singleton
class IO @Inject constructor(
    @ApplicationContext val context: Context,
) {
    val zip = Zip(context)

    class Zip(private val context: Context) {
        fun openZipInput(uri: Uri): ZipInputStream {
            val inputStream = try {
                context.contentResolver.openInputStream(uri)
            } catch (e: IOException) {
                Timber.Forest.d("Error opening zip at: $uri $e")
                null
            }

            return if (inputStream != null) {
                ZipInputStream(BufferedInputStream(inputStream))
            } else {
                error("Could not open zip file at $uri")
            }
        }

        fun openZipOutput(uri: Uri): ZipOutputStream {
            val out = context.contentResolver.openOutputStream(uri)
            return ZipOutputStream(out)
        }

        suspend fun writeZipEntry(
            filename: String,
            input: InputStream,
            zipOutputStream: ZipOutputStream,
        ): Result<Unit> = suspendCoroutine { continuation ->
            try {
                val entry = ZipEntry(filename)
                zipOutputStream.putNextEntry(entry)

                val bytesWritten = input.copyTo(zipOutputStream)
                input.close()
                zipOutputStream.closeEntry()

                if (bytesWritten <= 0) {
                    throw IOException("Failed writing bytes to zip entry for: $filename. Copied bytes: $bytesWritten")
                }

                continuation.resume(Result.success(Unit))
            } catch (e: IOException) {
                Timber.Forest.e(e, "Error writing zip entry for $filename")
                continuation.resume(Result.failure(e))
            }
        }
    }

    fun getFileName(uri: Uri): String? = try {
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
        context.contentResolver.query(uri, projection, null, null, null)?.use {
            if (it.moveToFirst()) {
                return it.getString(0)

            }
        }
        null
    } catch (e: SecurityException) {
        null
    }

    fun getFileSize(uri: Uri): Long {
        context.contentResolver.openFileDescriptor(uri, "r")?.use {
            return it.statSize
        }

        return -1L
    }

    /**
     * Copy [input] to [output], reporting every copied chunk to [onBytesCopied].
     */
    suspend fun copy(
        input: InputStream,
        output: OutputStream,
        onBytesCopied: (Long) -> Unit = {},
    ): Result<Long> = withContext(Dispatchers.IO) {
        suspendCoroutine { continuation ->
            try {
                var bytesWritten = 0L
                val buffer = ByteArray(COPY_BUFFER_SIZE)

                var read = input.read(buffer)
                while (read >= 0) {
                    output.write(buffer, 0, read)
                    bytesWritten += read
                    onBytesCopied(read.toLong())

                    read = input.read(buffer)
                }

                output.flush()
                output.close()

                continuation.resume(Result.success(bytesWritten))
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }
        }
    }

    fun openFileInput(fileUri: Uri): InputStream? =
        try {
            context.contentResolver.openInputStream(fileUri)
        } catch (e: Exception) {
            Timber.e("Error opening external file at $fileUri: $e")
            null
        }

    fun openFileOutput(fileUri: Uri): OutputStream? =
        try {
            context.contentResolver.openOutputStream(fileUri)
        } catch (e: Exception) {
            Timber.e("Error opening external file at $fileUri: $e")
            null
        }

    fun openFileOutput(
        contentResolver: ContentResolver,
        filename: String,
        mimeType: String,
        destinationUri: Uri
    ): OutputStream? {
        return try {
            DocumentFile.fromTreeUri(context, destinationUri)?.let { dir ->
                val newFile = dir.createFile(mimeType, filename)
                newFile?.uri?.let { contentResolver.openOutputStream(it) }
            }
        } catch (e: IOException) {
            Timber.e("Error opening external file at $destinationUri: $e")
            null
        }
    }

    fun deleteFile(fileUri: Uri): Boolean? =
        try {
            val srcDoc = DocumentFile.fromSingleUri(context, fileUri)
            srcDoc?.delete()
        } catch (e: IOException) {
            Timber.Forest.e("Error deleting external file at $fileUri: $e")
            null
        }
}