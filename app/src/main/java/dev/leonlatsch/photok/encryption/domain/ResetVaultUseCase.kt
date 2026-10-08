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

package dev.leonlatsch.photok.encryption.domain

import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.io.VaultCacheStorage
import dev.leonlatsch.photok.model.io.ThumbnailFiles
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.settings.data.Config
import javax.inject.Inject

class ResetVaultUseCase @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val albumRepository: AlbumRepository,
    private val config: Config,
    private val sessionRepository: SessionRepository,
    private val vaultCacheStorage: VaultCacheStorage,
    private val vaultProtectionRepository: VaultProtectionRepository,
) {
    suspend operator fun invoke() {
        val allPhotos = photoRepository.findAllPhotosByImportDateDesc()
        for (photo in allPhotos) {
            photoRepository.deleteInternalPhotoData(photo)
        }
        vaultCacheStorage.deleteDir(ThumbnailFiles.DIR)
        photoRepository.deleteAll()
        albumRepository.deleteAll()
        albumRepository.unlinkAll()

        vaultProtectionRepository.removeAllProtections()

        config.legacyPasswordHash = null
        config.legacyUserSalt = null

        sessionRepository.reset()
    }
}

class EraseVaultDataUseCase @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val albumRepository: AlbumRepository,
) {
    suspend operator fun invoke() {
        val allPhotos = photoRepository.findAllPhotosByImportDateDesc()
        for (photo in allPhotos) {
            photoRepository.deleteInternalPhotoData(photo)
        }
        photoRepository.deleteAll()
        albumRepository.deleteAll()
        albumRepository.unlinkAll()
    }
}