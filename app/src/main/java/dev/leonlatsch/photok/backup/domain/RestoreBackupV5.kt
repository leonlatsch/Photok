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

package dev.leonlatsch.photok.backup.domain

import dev.leonlatsch.photok.backup.data.toDomain
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.model.io.IO
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.security.AES_ALGORITHM
import dev.leonlatsch.photok.vaults.domain.VaultService
import timber.log.Timber
import java.util.zip.ZipInputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.inject.Inject

/**
 * Backup Format V5
 *
 *  A ZIP archive with the following structure:
 *
 *  ┌───────────────────────────────┐
 *  │           backup.zip          │
 *  ├───────────────────────────────┤
 *  │ meta.json                     │
 *  │   {                           │
 *  │     "vault": VaultBackup,     │
 *  │     "photos": [PhotoBackup],  │
 *  │     "albums": [AlbumBackup],  │
 *  │     "albumPhotoRefs":         │
 *  │        [AlbumPhotoRefBackup], │
 *  │     "createdAt": Long,        │
 *  │     "backupVersion": Int      │
 *  │   }                           │
 *  │                               │
 *  │ <uuid>.crypt                  │  ← Encrypted photo/video
 *  │ <uuid>.crypt.tn               │  ← Encrypted thumbnail
 *  │ <uuid>.crypt.vp               │  ← Encrypted video preview
 *  │ ...                           │
 *  └───────────────────────────────┘
 *
 * Notes:
 *  - `vault` is used to restore the backup as a vault. Or into the current vault.
 *  - `photos`, `albums`, and `albumPhotoRefs` define the logical structure.
 *  - Each media file is identified by a UUID and encrypted.
 *  - `createdAt` is the timestamp of backup creation.
 *  - `backupVersion` must equal 5 for this format.
 */
class RestoreBackupV5 @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val albumRepository: AlbumRepository,
    private val io: IO,
    private val vaultService: VaultService,
) : RestoreBackupStrategy {

    override suspend fun restore(
        metaData: BackupMetaData,
        stream: ZipInputStream,
        originalPassword: String
    ): RestoreResult {
        require(metaData is BackupMetaData.V5)

        val start = System.currentTimeMillis()

        var errors = 0

        var ze = stream.nextEntry

        val vault = metaData.vault.toDomain()

        val contentKey = vaultService.unlock(vault, originalPassword) .getOrNull()
        contentKey ?: return RestoreResult(errors)

        while (ze != null) {
            if (ze.name == META_JSON_FILENAME) {
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

            val cipher = Cipher.getInstance(AES_ALGORITHM).apply {
                init(Cipher.DECRYPT_MODE, contentKey)
            }
            CipherInputStream(stream, cipher).use { encryptedInput ->
                val encryptedOutput = io.openFileOutput(ze.name)
                io.copy(encryptedInput, encryptedOutput)
            }

            // Could directly copy if vault is also imported
//            io.openFileOutput(ze.name).use { out ->
//                io.copy(stream, out)
//            }

            ze = stream.nextEntry
        }

        metaData.getPhotosInOriginalOrder().forEach { photoBackup ->
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

        Timber.d("PERFORMANCE: Restore backup took ${System.currentTimeMillis() - start}ms")

        return RestoreResult(errors)
    }

}