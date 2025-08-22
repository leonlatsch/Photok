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

import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.backup.data.BackupMetaData
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import dev.leonlatsch.photok.model.io.IO
import dev.leonlatsch.photok.settings.data.Config
import java.io.ByteArrayInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class LegacyBackupStrategyImpl @Inject constructor(
    private val dumpDatabaseUseCase: DumpDatabaseUseCase,
    private val encryptedStorageManager: EncryptedStorageManager,
    private val io: IO,
    private val config: Config,
    private val gson: Gson,
    @ApplicationContext private val context: Context,
) : BackupStrategy {

    override suspend fun writePhotoToBackup(
        photo: Photo,
        zipOutputStream: ZipOutputStream
    ): Result<Unit> {
        context.fileList()
            .filter { it.contains(photo.uuid) && it.contains("photok") }
            .map { it to encryptedStorageManager.internalOpenFileInput(it) }
            .forEach { file ->
                val filename = file.first
                val inputStream = file.second

                inputStream
                    ?: return Result.failure(IllegalStateException("Input stream missing for photo"))

                io.zip.writeZipEntry(filename, inputStream, zipOutputStream)
                    .onFailure {
                        return Result.failure(it)
                    }
            }

        return Result.success(Unit)
    }

    override suspend fun createMetaFileInBackup(zipOutputStream: ZipOutputStream): Result<Unit> {
        val backupMetaData = dumpDatabaseUseCase(config.securityPassword!!, BackupMetaData.Companion.LEGACY_BACKUP_VERSION)

        val metaBytes = gson.toJson(backupMetaData).toByteArray()

        return io.zip.writeZipEntry(
            BackupMetaData.Companion.FILE_NAME,
            ByteArrayInputStream(metaBytes),
            zipOutputStream,
        )
    }
}