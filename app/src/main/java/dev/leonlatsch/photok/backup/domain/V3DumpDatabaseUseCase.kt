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

import dev.leonlatsch.photok.backup.data.toBackup
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.settings.data.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Dump the photok database into a [BackupMetaData.V3].
 * Needed for the backup created before the migration from .photok to .crypt
 */
class V3DumpDatabaseUseCase @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val albumRepository: AlbumRepository,
    private val config: Config,
) {
    suspend operator fun invoke(version: Int): BackupMetaData = withContext(Dispatchers.IO) {
        val photos = photoRepository.findAllPhotosByImportDateDesc().map { it.toBackup() }
        val albums = albumRepository.getAlbums().map { it.toBackup() }
        val albumPhotoLinks = albumRepository.getAllAlbumPhotoLinks().map { it.toBackup() }

        val password = config.passwordForMigration ?: error("Error while creating legacy backup. Password was not set")

        BackupMetaData.V3(
            password = password,
            photos = photos,
            albums = albums,
            albumPhotoRefs = albumPhotoLinks,
            backupVersion = version,
        )
    }
}