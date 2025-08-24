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
import dev.leonlatsch.photok.model.io.CreateThumbnailsUseCase
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.security.migration.LegacyEncryptionManager
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Backup Format V1
 *
 *  A ZIP archive with the following structure:
 *
 *  ┌───────────────────────────────┐
 *  │           backup.zip          │
 *  ├───────────────────────────────┤
 *  │ meta.json                     │
 *  │   {                           │
 *  │     "password": String,       │
 *  │     "salt": String?,          │
 *  │     "photos": [PhotoBackup],  │
 *  │     "createdAt": Long,        │
 *  │     "backupVersion": Int      │
 *  │   }                           │
 *  │                               │
 *  │ <uuid>.photok                 │  ← Encrypted photo/video
 *  │ ...                           │
 *  └───────────────────────────────┘
 *
 * Notes:
 *  - `password` and optional `salt` are used for decryption.
 *  - Only `photos` are tracked (no albums or albumPhotoRefs).
 *  - Media files are encrypted and stored as `<uuid>.photok`.
 *  - No thumbnails (`.tn`) or video previews (`.vp`) in this version.
 *  - `backupVersion` must equal 1 for this format.
 */
class RestoreBackupV1 @Inject constructor(
    @LegacyEncryptionManager private val legacyEncryptionManager: EncryptionManager,
    private val photoRepository: PhotoRepository,
    private val createThumbnails: CreateThumbnailsUseCase,
) : RestoreBackupStrategy {
    override suspend fun restore(
        metaData: BackupMetaData,
        stream: ZipInputStream,
        originalPassword: String,
    ): RestoreResult {
        var errors = 0

        var ze = stream.nextEntry

        while (ze != null) {
            val photoBackup = metaData.photos.find {
                ze.name.contains(it.uuid)
            }

            if (photoBackup == null) {
                ze = stream.nextEntry
                continue
            }

            val newPhoto = photoBackup.toDomain().copy(importedAt = System.currentTimeMillis())

            val encryptedZipInput =
                legacyEncryptionManager.createCipherInputStream(stream, originalPassword)
            if (encryptedZipInput == null) {
                ze = stream.nextEntry
                continue
            }

            val photoBytes = suspendCoroutine {
                it.resume(encryptedZipInput.readBytes())
            }
            val photoBytesInputStream = ByteArrayInputStream(photoBytes)

            val photoFileCreated =
                photoRepository.createPhotoFile(newPhoto, photoBytesInputStream) != -1L

            if (!photoFileCreated) {
                errors++
                ze = stream.nextEntry
                continue
            }

            createThumbnails(newPhoto, photoBytes)
                .onSuccess {
                    photoRepository.insert(newPhoto)
                }
                .onFailure {
                    errors++
                }

            ze = stream.nextEntry
        }

        return RestoreResult(errors)
    }
}