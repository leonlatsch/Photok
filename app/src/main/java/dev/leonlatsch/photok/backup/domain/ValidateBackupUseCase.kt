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

package dev.leonlatsch.photok.backup.domain

import android.net.Uri
import dev.leonlatsch.photok.backup.data.BackupMetaData
import dev.leonlatsch.photok.backup.data.ReadBackupMetadataUseCase
import dev.leonlatsch.photok.model.database.entity.LEGACY_PHOTOK_FILE_EXTENSION
import dev.leonlatsch.photok.model.database.entity.PHOTOK_FILE_EXTENSION
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.io.IO
import javax.inject.Inject

class ValidateBackupUseCase @Inject constructor(
    private val readBackupMetadata: ReadBackupMetadataUseCase,
    private val io: IO,
) {

    suspend operator fun invoke(uri: Uri): Result<BackupValidation> {
        val zipInputStream = io.zip.openZipInput(uri)

        var cryptFiles = 0
        var photokFiles = 0

        var metaData: BackupMetaData? = null
        var backupVersion: Int? = null

        var ze = zipInputStream.nextEntry
        while (ze != null) {
            if (ze.name == BackupMetaData.FILE_NAME) {
                metaData = readBackupMetadata(zipInputStream)
                backupVersion = metaData.getBackupVersion()
            } else if (ze.name.endsWith(PHOTOK_FILE_EXTENSION)) {
                cryptFiles++
            } else if (ze.name.endsWith(LEGACY_PHOTOK_FILE_EXTENSION)) {
                photokFiles++
            }

            ze = zipInputStream.nextEntry
        }

        if ((cryptFiles == 0) && (photokFiles == 0)) {
            return Result.failure(IllegalStateException("No crypt files or photok files found"))
        }

        if (metaData == null || backupVersion == null) {
            return Result.failure(IllegalStateException("No metadata found"))
        }

        val fileName = io.getFileName(uri)
        val fileSize = io.getFileSize(uri)

        val backupValidation = BackupValidation(
            metaData = metaData,
            fileName = fileName.orEmpty(),
            fileSize = fileSize,
        )

        return Result.success(backupValidation)
    }
}

private fun BackupMetaData?.getBackupVersion(): Int? {
    this?.let {
        return if (it.backupVersion == 0) { // Treat legacy version 0 as 1
            1
        } else {
            it.backupVersion
        }
    }

    return null
}

data class BackupValidation(
    val metaData: BackupMetaData,
    val fileName: String,
    val fileSize: Long,
)