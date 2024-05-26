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

package dev.leonlatsch.photok.backup.data

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.backup.domain.BackupRepository
import dev.leonlatsch.photok.backup.domain.model.BackupFileDetails
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import dev.leonlatsch.photok.other.extensions.empty
import dev.leonlatsch.photok.other.getFileName
import dev.leonlatsch.photok.other.getFileSize
import timber.log.Timber
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class BackupRepositoryImpl @Inject constructor(
    private val encryptedStorageManager: EncryptedStorageManager,
    private val backupLocalDataSource: BackupLocalDataSource,
    private val gson: Gson,
    @ApplicationContext private val context: Context,
) : BackupRepository {

    override suspend fun openBackupInput(uri: Uri): ZipInputStream {
        val inputStream = try {
            context.contentResolver.openInputStream(uri)
        } catch (e: IOException) {
            Timber.d("Error opening backup at: $uri")
            null
        }

        return if (inputStream != null) {
            ZipInputStream(BufferedInputStream(inputStream))
        } else {
            error("Could not open zip file at $uri")
        }
    }

    override suspend fun openBackupOutput(uri: Uri): ZipOutputStream {
        val out = context.contentResolver.openOutputStream(uri)
        return ZipOutputStream(out)
    }

    override suspend fun writePhoto(
        photo: Photo,
        zipOutputStream: ZipOutputStream,
    ): Result<Unit> {
        val fileInputs = buildList {
            val file = encryptedStorageManager.internalOpenFileInput(photo.internalFileName)
            add(photo.internalFileName to file)

            val thumbnail =
                encryptedStorageManager.internalOpenFileInput(photo.internalThumbnailFileName)
            add(photo.internalThumbnailFileName to thumbnail)

            if (photo.type.isVideo) {
                val videoPreview =
                    encryptedStorageManager.internalOpenFileInput(photo.internalVideoPreviewFileName)
                add(photo.internalVideoPreviewFileName to videoPreview)
            }
        }

        fileInputs.forEach { file ->
            val filename = file.first
            val inputStream = file.second

            inputStream ?: return Result.failure(IllegalStateException("Input stream missing for photo"))

            backupLocalDataSource.writeZipEntry(filename, inputStream, zipOutputStream)
                .onFailure {
                    return Result.failure(it)
                }
        }

        return Result.success(Unit)
    }

    override suspend fun writeBackupMetadata(
        backupMetaData: BackupMetaData,
        zipOutputStream: ZipOutputStream
    ) {
        val metaBytes = gson.toJson(backupMetaData).toByteArray()

        backupLocalDataSource.writeZipEntry(
            BackupMetaData.FILE_NAME,
            ByteArrayInputStream(metaBytes),
            zipOutputStream,
        )
    }

    override suspend fun readBackupMetadata(zipInputStream: ZipInputStream): BackupMetaData {
        val bytes = zipInputStream.readBytes()
        val string = String(bytes)

        val metaData = gson.fromJson(string, BackupMetaData::class.java)
        metaData ?: error("Error reading meta json from $zipInputStream")

        return metaData
    }

    override suspend fun getBackupFileDetails(uri: Uri): BackupFileDetails {
        val fileDetails = BackupFileDetails(
            filename = getFileName(context.contentResolver, uri) ?: String.empty,
            fileSize = getFileSize(context.contentResolver, uri),
        )

        return fileDetails
    }
}