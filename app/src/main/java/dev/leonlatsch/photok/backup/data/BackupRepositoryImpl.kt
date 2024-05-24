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

import com.google.gson.Gson
import dev.leonlatsch.photok.backup.domain.BackupRepository
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import java.io.ByteArrayInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class BackupRepositoryImpl @Inject constructor(
    private val encryptedStorageManager: EncryptedStorageManager,
    private val backupLocalDataSource: BackupLocalDataSource,
    private val gson: Gson,
) : BackupRepository {

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
}