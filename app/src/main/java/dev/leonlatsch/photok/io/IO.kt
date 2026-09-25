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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val COPY_BUFFER_SIZE = 8192

/** `PK\x05\x06`, the marker that opens a zip's end of central directory record. */
private val END_OF_CENTRAL_DIRECTORY_SIGNATURE = byteArrayOf(0x50, 0x4B, 0x05, 0x06)

/** The record is 22 bytes and may be followed by a comment of up to 65535 bytes. */
private const val END_OF_CENTRAL_DIRECTORY_MAX_OFFSET = 22 + 65535

@Singleton
class IO @Inject constructor(
    @ApplicationContext val context: Context,
) {
    val zip = Zip(context)

    class Zip(private val context: Context) {
        /** Returns `null` when the zip can not be opened. */
        fun openZipInput(uri: Uri): ZipInputStream? {
            val inputStream = try {
                context.contentResolver.openInputStream(uri)
            } catch (e: Exception) {
                Timber.e(e, "Error opening zip at: $uri")
                null
            }

            if (inputStream == null) {
                Timber.e("Could not open zip file at $uri")
                return null
            }

            return ZipInputStream(BufferedInputStream(inputStream))
        }

        /** Returns `null` when the zip can not be opened. */
        fun openZipOutput(uri: Uri): ZipOutputStream? {
            val out = try {
                context.contentResolver.openOutputStream(uri)
            } catch (e: Exception) {
                Timber.e(e, "Error opening zip output at: $uri")
                null
            }

            if (out == null) {
                Timber.e("Could not open zip output at $uri")
                return null
            }

            return ZipOutputStream(BufferedOutputStream(out))
        }

        /**
         * Whether the file ends in a zip end of central directory record.
         *
         * A zip writes its directory last, so a file that is missing it was cut short — an
         * interrupted download or copy. Reading the entries can not tell that apart from a
         * genuinely short archive, this can, and it only costs a seek to the end.
         *
         * Returns `true` when the file can not be inspected, so an unreadable size never blocks
         * a restore on its own.
         */
        fun hasEndOfCentralDirectory(uri: Uri): Boolean = try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                val size = descriptor.statSize

                if (size <= 0) {
                    true
                } else {
                    val tailSize = minOf(size, END_OF_CENTRAL_DIRECTORY_MAX_OFFSET.toLong()).toInt()
                    val tail = ByteBuffer.allocate(tailSize)

                    FileInputStream(descriptor.fileDescriptor).use { input ->
                        val channel = input.channel
                        channel.position(size - tailSize)
                        while (tail.hasRemaining() && channel.read(tail) >= 0) Unit
                    }

                    tail.array().containsSignature(END_OF_CENTRAL_DIRECTORY_SIGNATURE)
                }
            } ?: true
        } catch (e: Exception) {
            Timber.e(e, "Could not check the zip directory of $uri")
            true
        }

        /**
         * Writes [input] as one entry. Cancelable between chunks, so aborting a backup stops
         * inside a large video instead of at the next photo. [input] is closed either way.
         */
        suspend fun writeZipEntry(
            filename: String,
            input: InputStream,
            zipOutputStream: ZipOutputStream,
            onBytesCopied: (Long) -> Unit = {},
        ): Result<Unit> = withContext(Dispatchers.IO) {
            try {
                zipOutputStream.putNextEntry(ZipEntry(filename))

                input.use { input ->
                    val buffer = ByteArray(COPY_BUFFER_SIZE)
                    var read = input.read(buffer)

                    while (read >= 0) {
                        ensureActive()

                        zipOutputStream.write(buffer, 0, read)
                        onBytesCopied(read.toLong())

                        read = input.read(buffer)
                    }
                }

                zipOutputStream.closeEntry()

                Result.success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "Error writing zip entry for $filename")
                Result.failure(e)
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

    /** Free space left in the internal files directory, where the vault lives. */
    fun usableInternalBytes(): Long = try {
        context.filesDir.usableSpace
    } catch (e: Exception) {
        Timber.e(e, "Could not read the usable space of the files dir")
        Long.MAX_VALUE
    }

    /**
     * Copy [input] to [output], reporting every copied chunk to [onBytesCopied].
     *
     * Cancelable between chunks, so canceling the caller stops the copy instead of running it to
     * the end. [output] is closed either way, [input] is left open because it can be a stream the
     * caller keeps reading from, like a single entry of a [ZipInputStream].
     */
    suspend fun copy(
        input: InputStream,
        output: OutputStream,
        onBytesCopied: (Long) -> Unit = {},
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            var bytesWritten = 0L
            val buffer = ByteArray(COPY_BUFFER_SIZE)

            output.use { output ->
                var read = input.read(buffer)
                while (read >= 0) {
                    ensureActive()

                    output.write(buffer, 0, read)
                    bytesWritten += read
                    onBytesCopied(read.toLong())

                    read = input.read(buffer)
                }

                output.flush()
            }

            Result.success(bytesWritten)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
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
            Timber.e("Error deleting external file at $fileUri: $e")
            null
        }
}

private fun ByteArray.containsSignature(signature: ByteArray): Boolean {
    outer@ for (start in 0..size - signature.size) {
        for (offset in signature.indices) {
            if (this[start + offset] != signature[offset]) continue@outer
        }
        return true
    }

    return false
}
