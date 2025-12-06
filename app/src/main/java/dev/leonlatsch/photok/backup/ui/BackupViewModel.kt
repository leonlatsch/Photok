/*
 *   Copyright 2020-2022 Leon Latsch
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

package dev.leonlatsch.photok.backup.ui

import android.app.Application
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.backup.domain.BackupStrategy
import dev.leonlatsch.photok.backup.domain.BackupStrategyImpl
import dev.leonlatsch.photok.backup.domain.LegacyBackupStrategyImpl
import dev.leonlatsch.photok.backup.domain.UnEncryptedBackupStrategy
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.io.IO
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.other.extensions.lazyClose
import dev.leonlatsch.photok.uicomponnets.base.processdialogs.BaseProcessViewModel
import timber.log.Timber
import java.util.zip.ZipOutputStream
import javax.inject.Inject

/**
 * ViewModel to create a backup.
 * Backups photos and meta data to zip file.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class BackupViewModel @Inject constructor(
    app: Application,
    private val photoRepository: PhotoRepository,
    private val io: IO,
    private val defaultBackupStrategy: BackupStrategyImpl,
    private val legacyBackupStrategy: LegacyBackupStrategyImpl,
    private val unEncryptedBackupStrategy: UnEncryptedBackupStrategy,
) : BaseProcessViewModel<Photo>(app) {

    lateinit var uri: Uri

    lateinit var strategyName: BackupStrategy.Name

    private val strategy: BackupStrategy by lazy {
        when (strategyName) {
            BackupStrategy.Name.Default -> defaultBackupStrategy
            BackupStrategy.Name.Legacy -> legacyBackupStrategy
            BackupStrategy.Name.UnEncrypted -> unEncryptedBackupStrategy
        }
    }

    private lateinit var zipOutputStream: ZipOutputStream

    override suspend fun preProcess() {
        items = photoRepository.findAllPhotosByImportDateDesc()
        elementsToProcess = items.size
        zipOutputStream = io.zip.openZipOutput(uri)
        strategy.preBackup()
        super.preProcess()
    }

    override suspend fun processItem(item: Photo) {
        strategy.writePhotoToBackup(item, zipOutputStream)
            .onFailure {
                Timber.e(it, "Error writing photo to backup")
                failuresOccurred = true
            }
    }

    override suspend fun postProcess() {
        if (failuresOccurred.not()) {
            strategy.createMetaFileInBackup(zipOutputStream)
                .onFailure {
                    Timber.e(it, "Error writing meta file to backup")
                    failuresOccurred = true
                }
        }

        zipOutputStream.lazyClose()
        strategy.postBackup()
        super.postProcess()
    }
}
