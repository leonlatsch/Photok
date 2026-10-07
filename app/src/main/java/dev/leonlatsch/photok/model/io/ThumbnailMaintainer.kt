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

package dev.leonlatsch.photok.model.io

import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.io.VaultCacheStorage
import dev.leonlatsch.photok.io.VaultFileStorage
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

/**
 * Gives the gallery time to load its first thumbnails before competing for CPU.
 */
private val StartDelay = 3.seconds

/**
 * Brings the thumbnails in the cache dir in line with the vault, once per unlock.
 */
@Singleton
class ThumbnailMaintainer @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val photoRepository: PhotoRepository,
    private val vaultFileStorage: VaultFileStorage,
    private val vaultCacheStorage: VaultCacheStorage,
    private val thumbnailGenerator: ThumbnailGenerator,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start() {
        scope.launch {
            sessionRepository.observe().collectLatest { session ->
                if (session == null) return@collectLatest

                delay(StartDelay)
                run()
            }
        }
    }

    internal suspend fun run() {
        val start = System.currentTimeMillis()

        deleteOutdatedVersions()

        val photos = photoRepository.findAllPhotosByImportDateDesc()
        deleteDeadThumbnails(photos, start)
        createMissingThumbnails(photos)
    }

    private fun deleteOutdatedVersions() {
        vaultCacheStorage.listFiles(ThumbnailFiles.DIR)
            .filter { "${ThumbnailFiles.DIR}/${it.name}" != ThumbnailFiles.CURRENT_DIR }
            .forEach {
                Timber.i("Deleting outdated thumbnails: ${it.name}")
                vaultCacheStorage.deleteDir("${ThumbnailFiles.DIR}/${it.name}")
            }
    }

    /**
     * Files created after [runStart] are kept, because imports create the thumbnail before the photo is saved.
     */
    private fun deleteDeadThumbnails(photos: List<Photo>, runStart: Long) {
        val uuids = photos.map { it.uuid }.toSet()

        vaultCacheStorage.listFiles(ThumbnailFiles.CURRENT_DIR)
            .filter { ThumbnailFiles.uuidOf(it) !in uuids && it.lastModified() < runStart }
            .forEach {
                Timber.i("Deleting dead thumbnail: ${it.name}")
                vaultCacheStorage.deleteEncryptedFile(ThumbnailFiles.pathInCurrentDir(it))
            }
    }

    /**
     * A legacy thumbnail in the vault files is only deleted once its replacement exists.
     */
    private suspend fun createMissingThumbnails(photos: List<Photo>) {
        for (photo in photos) {
            val result = thumbnailGenerator.createFromVault(photo.uuid, photo.type)
            if (result.isFailure) {
                Timber.w(result.exceptionOrNull(), "Could not create thumbnail for ${photo.uuid}")
                continue
            }

            if (vaultFileStorage.encryptedFileExists(photo.internalThumbnailFileName)) {
                vaultFileStorage.deleteEncryptedFile(photo.internalThumbnailFileName)
            }
        }
    }
}
