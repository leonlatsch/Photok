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

package dev.leonlatsch.photok.model.repositories

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.io.VaultFileStorage
import dev.leonlatsch.photok.model.database.entity.LEGACY_PHOTOK_FILE_EXTENSION
import dev.leonlatsch.photok.model.database.entity.PHOTOK_FILE_EXTENSION
import dev.leonlatsch.photok.model.database.entity.THUMBNAIL_SUFFIX
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CleanupDeadFilesUseCase @Inject constructor(
    private val photoRepository: PhotoRepository,
    @ApplicationContext private val context: Context,
    private val vaultFileStorage: VaultFileStorage,
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    operator fun invoke() {
        scope.launch {
            val allExisting = photoRepository.findAllPhotosByImportDateDesc()

            val allFiles = context.fileList().filter {
                val isLegacyVaultFile = it.contains(LEGACY_PHOTOK_FILE_EXTENSION)
                val isVaultFile = it.contains(PHOTOK_FILE_EXTENSION)
                val isThumbnail = it.endsWith(THUMBNAIL_SUFFIX)

                (isLegacyVaultFile || isVaultFile) && isThumbnail.not()
            }

            for (file in allFiles) {
                val uuid  = file.substringBefore(".")

                if (allExisting.none { uuid == it.uuid }) {
                    Timber.i("Deleting dead file: $file")
                    vaultFileStorage.deleteEncryptedFile(file)
                }
            }

            val oldThumbnails = context.fileList().filter {
                it.endsWith("$PHOTOK_FILE_EXTENSION$THUMBNAIL_SUFFIX")
            }

            for (file in oldThumbnails) {
                Timber.i("Deleting old thumbnail: $file")
                vaultFileStorage.deleteEncryptedFile(file)
            }
        }
    }
}