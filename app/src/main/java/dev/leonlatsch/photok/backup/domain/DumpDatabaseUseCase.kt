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

import dev.leonlatsch.photok.backup.data.BackupMetaData
import dev.leonlatsch.photok.backup.data.BackupMetaData.Companion.CURRENT_BACKUP_VERSION
import dev.leonlatsch.photok.backup.data.BackupMetaData.Companion.LEGACY_BACKUP_VERSION
import dev.leonlatsch.photok.backup.data.toBackup
import dev.leonlatsch.photok.encryption.domain.VaultProtectionRepository
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionType
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.settings.data.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.io.encoding.Base64

/**
 * Creates a [BackupMetaData] from the current database
 */
class DumpDatabaseUseCase @Inject constructor(
    private val config: Config,
    private val photoRepository: PhotoRepository,
    private val albumRepository: AlbumRepository,
    private val vaultProtectionRepository: VaultProtectionRepository,
) {
    suspend operator fun invoke(version: Int): BackupMetaData = withContext(Dispatchers.IO) {
        val photos = photoRepository.findAllPhotosByImportDateDesc().map { it.toBackup() }
        val albums = albumRepository.getAlbums().map { it.toBackup() }
        val albumPhotoLinks = albumRepository.getAllAlbumPhotoLinks().map { it.toBackup() }

        when (version) {
            LEGACY_BACKUP_VERSION -> {
                BackupMetaData.V3(
                    password = requireNotNull(config.legacyPasswordHash) { "Legacy password hash is required for V3 backup" },
                    photos = photos,
                    albums = albums,
                    albumPhotoRefs = albumPhotoLinks,
                    backupVersion = version,
                )
            }
            CURRENT_BACKUP_VERSION -> {
                val protection = vaultProtectionRepository.getProtection(VaultProtectionType.Password)
                requireNotNull(protection)

                BackupMetaData.V5(
                    wrappedVMK = Base64.encode(protection.wrappedVMK),
                    params = protection.params,
                    photos = photos,
                    albums = albums,
                    albumPhotoRefs = albumPhotoLinks,
                    backupVersion = version,
                )
            }
            else -> error("Not supported to create backup metadata with version $version")
        }
    }
}