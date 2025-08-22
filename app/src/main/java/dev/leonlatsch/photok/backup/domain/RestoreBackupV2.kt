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

import dev.leonlatsch.photok.backup.data.BackupMetaData
import dev.leonlatsch.photok.backup.data.toDomain
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import dev.leonlatsch.photok.model.io.IO
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.security.LegacyEncryptionManagerImpl
import timber.log.Timber
import java.util.zip.ZipInputStream
import javax.inject.Inject

class RestoreBackupV2 @Inject constructor(
    private val legacyEncryptionManager: LegacyEncryptionManagerImpl,
    private val encryptedStorageManager: EncryptedStorageManager,
    private val photoRepository: PhotoRepository,
    private val io: IO,
) : RestoreBackupStrategy {

    override suspend fun restore(
        metaData: BackupMetaData,
        stream: ZipInputStream,
        originalPassword: String
    ): RestoreResult {
        var errors = 0

        var ze = stream.nextEntry

        while (ze != null) {
            if (ze.name == BackupMetaData.FILE_NAME) {
                ze = stream.nextEntry
                continue
            }

            val encryptedZipInput =
                legacyEncryptionManager.createCipherInputStream(stream, originalPassword)
            val internalOutputStream =
                encryptedStorageManager.internalOpenEncryptedFileOutput(ze.name)

            if (encryptedZipInput == null || internalOutputStream == null) {
                ze = stream.nextEntry
                continue
            }

            io.copy(encryptedZipInput, internalOutputStream)
                .onFailure {
                    Timber.e(it, "Error restoring zip entry: ${ze.name}")
                    errors++
                }

            ze = stream.nextEntry
        }

        metaData.photos.forEach { photoBackup ->
            val newPhoto = photoBackup
                .toDomain()
                .copy(importedAt = System.currentTimeMillis())

            photoRepository.insert(newPhoto)
        }

        return RestoreResult(errors)
    }
}