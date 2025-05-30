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
import dev.leonlatsch.photok.backup.domain.BackupRepository
import dev.leonlatsch.photok.backup.domain.CreateBackupMetaFileUseCase
import dev.leonlatsch.photok.model.database.entity.Photo
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
    private val backupRepository: BackupRepository,
    private val createBackupMetaFile: CreateBackupMetaFileUseCase,
) : BaseProcessViewModel<Photo>(app) {

    lateinit var uri: Uri
    private lateinit var zipOutputStream: ZipOutputStream

    override suspend fun preProcess() {
        items = photoRepository.getAll()
        elementsToProcess = items.size
        zipOutputStream = backupRepository.openBackupOutput(uri)
        super.preProcess()
    }

    override suspend fun processItem(item: Photo) {
        backupRepository.writePhoto(item, zipOutputStream)
            .onFailure {
                Timber.e(it, "Error writing photo to backup")
                failuresOccurred = true
            }
    }

    override suspend fun postProcess() {
        if (failuresOccurred.not()) {
            createBackupMetaFile(zipOutputStream)
                .onFailure {
                    Timber.e(it, "Error writing meta file to backup")
                    failuresOccurred = true
                }
        }

        zipOutputStream.lazyClose()
        super.postProcess()
    }
}
