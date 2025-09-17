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
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.model.database.entity.LEGACY_PHOTOK_FILE_EXTENSION
import dev.leonlatsch.photok.model.database.entity.PHOTOK_FILE_EXTENSION
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import dev.leonlatsch.photok.model.io.IO
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.security.migration.LegacyEncryptionManager
import timber.log.Timber
import java.util.zip.ZipInputStream
import javax.inject.Inject

/**
 * Backup Format V3
 *
 *  A ZIP archive with the following structure:
 *
 *  ┌───────────────────────────────┐
 *  │           backup.zip          │
 *  ├───────────────────────────────┤
 *  │ meta.json                     │
 *  │   {                           │
 *  │     "password": String,       │
 *  │     "photos": [PhotoBackup],  │
 *  │     "albums": [AlbumBackup],  │
 *  │     "albumPhotoRefs":         │
 *  │        [AlbumPhotoRefBackup], │
 *  │     "createdAt": Long,        │
 *  │     "backupVersion": Int      │
 *  │   }                           │
 *  │                               │
 *  │ <uuid>.photok                 │  ← Encrypted photo/video
 *  │ <uuid>.photok.tn              │  ← Encrypted thumbnail
 *  │ <uuid>.photok.vp              │  ← Encrypted video preview
 *  │ ...                           │
 *  └───────────────────────────────┘
 *
 * Notes:
 *  - `password` is used to check before decryption.
 *  - `photos`, `albums`, and `albumPhotoRefs` define the logical structure.
 *  - Each media file is identified by a UUID and encrypted.
 *  - File extension differs from V4: uses `.photok.*` instead of `.crypt.*`.
 *  - `backupVersion` must equal 3 for this format.
 */
class RestoreBackupV3 @Inject constructor(
    @LegacyEncryptionManager private val legacyEncryptionManager: EncryptionManager,
    private val encryptedStorageManager: EncryptedStorageManager,
    private val photoRepository: PhotoRepository,
    private val albumRepository: AlbumRepository,
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

            // Skip files that are not mentioned in the metadata
            // These might be dead files from old versions of photok
            if (metaData.photos.none { ze.name.contains(it.uuid) }) {
                ze = stream.nextEntry
                Timber.i("Skipping dead file in backup: ${ze.name}")
                continue
            }

            val encryptedZipInput =
                legacyEncryptionManager.createCipherInputStream(stream, originalPassword)
            val internalOutputStream =
                encryptedStorageManager.internalOpenEncryptedFileOutput(
                    ze.name.replace(
                        oldValue = LEGACY_PHOTOK_FILE_EXTENSION,
                        newValue = PHOTOK_FILE_EXTENSION,
                    )
                )

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

        metaData.albums.forEach { albumBackup ->
            val album = albumBackup.toDomain()
            albumRepository.createAlbum(album)
        }

        metaData.albumPhotoRefs.forEach { albumPhotoRefBackup ->
            val albumPhotoRef = albumPhotoRefBackup.toDomain()
            albumRepository.link(albumPhotoRef)
        }

        return RestoreResult(errors)
    }

}
