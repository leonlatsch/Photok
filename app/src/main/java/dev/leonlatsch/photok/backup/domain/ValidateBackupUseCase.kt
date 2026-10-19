/*
 *   Copyright 2020–2026 Leon Latsch
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
import dev.leonlatsch.photok.io.IO
import dev.leonlatsch.photok.model.database.entity.LEGACY_PHOTOK_FILE_EXTENSION
import dev.leonlatsch.photok.model.database.entity.PHOTOK_FILE_EXTENSION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class ValidateBackupUseCase @Inject constructor(
    private val readBackupMetadata: ReadBackupMetadataUseCase,
    private val io: IO,
) {

    suspend operator fun invoke(uri: Uri): Result<BackupValidation> = withContext(Dispatchers.IO) {
        try {
            // Reading the archive can not tell a complete file from one whose tail is missing,
            // so the zip directory at the end is checked first. Costs one seek.
            if (!io.zip.hasEndOfCentralDirectory(uri)) {
                return@withContext Result.failure(BackupValidationError.IncompleteFile())
            }

            val metaData = readMetaData(uri)
                ?: return@withContext Result.failure(BackupValidationError.NoMetaData())

            if (metaData.photos.isEmpty() && !containsBackupFiles(uri)) {
                return@withContext Result.failure(BackupValidationError.NoBackupFiles())
            }

            val backupValidation = BackupValidation(
                metaData = metaData,
                fileName = io.getFileName(uri).orEmpty(),
                fileSize = io.getFileSize(uri),
                requiredBytes = metaData.photos.sumOf { it.size },
                usableBytes = io.usableInternalBytes(),
            )

            return@withContext Result.success(backupValidation)
        } catch (e: BackupValidationError) {
            Timber.e(e)
            return@withContext Result.failure(e)
        } catch (e: Exception) {
            Timber.e(e)
            return@withContext Result.failure(BackupValidationError.Unknown(e))
        }
    }

    /**
     * Backups written since meta.json became the first entry are read with a single entry;
     * older ones put it last, which means walking the whole archive.
     */
    private suspend fun readMetaData(uri: Uri): BackupMetaData? {
        val zipInputStream = io.zip.openZipInput(uri)
            ?: throw BackupValidationError.CannotOpenFile()

        zipInputStream.use { stream ->
            var entry = stream.nextEntry

            while (entry != null) {
                if (entry.name == BackupMetaData.FILE_NAME) {
                    return readBackupMetadata(stream)
                }

                entry = stream.nextEntry
            }
        }

        return null
    }

    /**
     * Only needed for the odd case of a meta.json that declares no photos at all. A backup of an
     * empty vault is valid, one whose metadata got truncated to an empty list is not.
     */
    private fun containsBackupFiles(uri: Uri): Boolean {
        val zipInputStream = io.zip.openZipInput(uri)
            ?: throw BackupValidationError.CannotOpenFile()

        zipInputStream.use { stream ->
            var entry = stream.nextEntry

            while (entry != null) {
                val name = entry.name
                if (name.endsWith(PHOTOK_FILE_EXTENSION) ||
                    name.endsWith(LEGACY_PHOTOK_FILE_EXTENSION)
                ) {
                    return true
                }

                entry = stream.nextEntry
            }
        }

        return false
    }
}

data class BackupValidation(
    val metaData: BackupMetaData,
    val fileName: String,
    val fileSize: Long,
    val requiredBytes: Long,
    val usableBytes: Long,
) {
    val notEnoughSpace: Boolean = requiredBytes > usableBytes
}

sealed class BackupValidationError(message: String) : Exception(message) {

    class CannotOpenFile : BackupValidationError("Could not open backup")

    class IncompleteFile : BackupValidationError("Backup file is incomplete")

    class NoBackupFiles : BackupValidationError("No crypt files or photok files found")

    class NoMetaData : BackupValidationError("No metadata found")

    data class UnsupportedVersion(val version: Int) :
        BackupValidationError("Backup version $version is newer than this app supports")

    data class Unknown(override val cause: Throwable) : BackupValidationError("Validation failed")
}
