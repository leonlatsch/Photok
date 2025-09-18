/*
 *   Copyright 2020-2024 Leon Latsch
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

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
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
                Timber.d("Error opening zip at: $uri $e")
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
                Timber.e(e, "Error writing zip entry for $filename")
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

    suspend fun copy(
        input: InputStream,
        output: OutputStream
    ): Result<Long> = suspendCoroutine { continuation ->
        try {
            val bytesWritten = input.copyTo(output, bufferSize = 8192)
            output.flush()
            output.close()

            continuation.resume(Result.success(bytesWritten))
        } catch (e: Exception) {
            continuation.resume(Result.failure(e))
        }
    }
}
