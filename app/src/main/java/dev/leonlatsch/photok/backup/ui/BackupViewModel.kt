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

package dev.leonlatsch.photok.backup.ui

import android.app.Application
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.backup.domain.BackupStrategy
import dev.leonlatsch.photok.backup.domain.BackupStrategyImpl
import dev.leonlatsch.photok.backup.domain.LegacyBackupStrategyImpl
import dev.leonlatsch.photok.encryption.domain.VaultProtectionRepository
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionType
import dev.leonlatsch.photok.io.IO
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.other.extensions.lazyClose
import dev.leonlatsch.photok.uicomponnets.base.processdialogs.BaseProcessViewModel
import dev.leonlatsch.photok.uicomponnets.base.processdialogs.ProcessState
import timber.log.Timber
import java.util.zip.Deflater
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
    private val vaultProtectionRepository: VaultProtectionRepository,
) : BaseProcessViewModel<Photo>(app) {

    lateinit var uri: Uri

    lateinit var strategyName: BackupStrategy.Name

    private val strategy: BackupStrategy by lazy {
        when (strategyName) {
            BackupStrategy.Name.Default -> defaultBackupStrategy
            BackupStrategy.Name.Legacy -> legacyBackupStrategy
        }
    }

    private var zipOutputStream: ZipOutputStream? = null

    // Indicates a fatal unrecoverable error. Backup should be canceled and deleted
    private var fatalFailure = false

    override suspend fun preProcess() {
        items = photoRepository.findAllPhotosByImportDateDesc()
        elementsToProcess = items.size

        val zip = io.zip.openZipOutput(uri)
        if (zip == null) {
            Timber.e("Could not open the backup file for writing")
            fail()
            return
        }

        // Compressing has no effect on encrypted files. This saves CPU
        zip.setLevel(Deflater.NO_COMPRESSION)
        zipOutputStream = zip

        // Should not because password is confirmed by user before
        val protection = vaultProtectionRepository.getProtection(VaultProtectionType.Password)
        if (protection == null) {
            Timber.e("No password protection found, cannot write a backup")
            fail()
            return
        }

        strategy.createMetaFileInBackup(zip)
            .onFailure {
                Timber.e(it, "Error writing meta file to backup")
                fail()
                return
            }

        super.preProcess()
    }

    override suspend fun processItem(item: Photo) {
        val zip = zipOutputStream ?: return

        strategy.writePhotoToBackup(item, zip)
            .onFailure {
                Timber.e(it, "Error writing photo to backup")
                failuresOccurred = true
            }
    }

    override suspend fun postProcess() {
        zipOutputStream?.lazyClose()

        // meta.json already in zip file. Delete backup
        if (fatalFailure || processState == ProcessState.ABORTED) {
            io.deleteFile(uri)
        }

        super.postProcess()
    }

    override fun cancel() {
        super.cancel()
        processingJob?.cancel()
    }

    private fun fail() {
        fatalFailure = true
        failuresOccurred = true
        cancel()
    }
}
