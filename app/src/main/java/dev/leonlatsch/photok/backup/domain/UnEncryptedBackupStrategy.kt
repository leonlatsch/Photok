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
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.model.database.entity.LEGACY_PHOTOK_FILE_EXTENSION
import dev.leonlatsch.photok.model.database.entity.PHOTOK_FILE_EXTENSION
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.io.IO
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.security.migration.LegacyEncryptionManager
import java.io.FileInputStream
import java.io.InputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class UnEncryptedBackupStrategy @Inject constructor(
    @ApplicationContext private val context: Context,
    private val io: IO,
    private val encryptionManager: EncryptionManager,
    @LegacyEncryptionManager private val legacyEncryptionManager: EncryptionManager,
) : BackupStrategy {

    override suspend fun writePhotoToBackup(
        photo: Photo,
        zipOutputStream: ZipOutputStream
    ): Result<Unit> {

        val input = getInputStreamForPhoto(photo)

        input ?: return Result.failure(IllegalStateException("Input stream missing for photo"))

        return io.zip.writeZipEntry(photo.fileName, input, zipOutputStream)
    }

    override suspend fun createMetaFileInBackup(zipOutputStream: ZipOutputStream): Result<Unit> {
        return Result.success(Unit)
    }

    private fun getInputStreamForPhoto(photo: Photo): InputStream? {
        val allFiles = context.fileList()

        for (file in allFiles) {
            if (!file.contains(photo.uuid)) {
                continue
            }

            if (file.endsWith(LEGACY_PHOTOK_FILE_EXTENSION)) {
                val encryptedInput =  context.openFileInput(file)
                return legacyEncryptionManager.createCipherInputStream(encryptedInput)
            }

            if (file.endsWith(PHOTOK_FILE_EXTENSION)) {
                val encryptedInput =  context.openFileInput(file)
                return encryptionManager.createCipherInputStream(encryptedInput)
            }

        }

        return null
    }
}