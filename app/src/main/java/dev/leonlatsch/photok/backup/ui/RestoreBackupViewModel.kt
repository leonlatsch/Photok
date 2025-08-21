/*
 *   Copyright 2020-2021 Leon Latsch
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
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.backup.data.BackupMetaData
import dev.leonlatsch.photok.backup.domain.BackupRepository
import dev.leonlatsch.photok.backup.domain.GetBackupRestoreStrategyUseCase
import dev.leonlatsch.photok.backup.domain.ValidateBackupUseCase
import dev.leonlatsch.photok.other.extensions.empty
import dev.leonlatsch.photok.other.extensions.lazyClose
import dev.leonlatsch.photok.uicomponnets.bindings.ObservableViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for loading and validating a backup file.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class RestoreBackupViewModel @Inject constructor(
    app: Application,
    private val getRestoreStrategy: GetBackupRestoreStrategyUseCase,
    private val backupRepository: BackupRepository,
    private val validateBackup: ValidateBackupUseCase,
) : ObservableViewModel(app) {

    @Bindable
    var restoreState: RestoreState = RestoreState.INITIALIZE
        set(value) {
            field = value
            notifyChange(BR.restoreState, value)
        }

    /**
     * [BackupMetaData] holding meta data of the loaded backup.
     */
    @get:Bindable
    var metaData: BackupMetaData? = null
        set(value) {
            field = value
            notifyChange(BR.metaData, value)
        }

    /**
     * File name of the zip.
     */
    @get:Bindable
    var zipFileName: String = String.empty
        set(value) {
            field = value
            notifyChange(BR.zipFileName, value)
        }

    @get:Bindable
    var backupSize: Long = 0
        set(value) {
            field = value
            notifyChange(BR.backupSize, value)
        }

    private lateinit var fileUri: Uri
    private var backupVersion = -1

    /**
     * Load and Validate a backup file. Fill [metaData].
     */
    fun loadAndValidateBackup(uri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        fileUri = uri

        validateBackup(uri)
            .onFailure { restoreState = RestoreState.FILE_INVALID }
            .onSuccess {
                restoreState = RestoreState.FILE_VALID


                backupSize = it.backupFileDetails.fileSize
                zipFileName = it.backupFileDetails.filename
                metaData = it.metaData
            }
    }

    /**
     * Restore the validated backup with the original password.
     */
    fun restoreBackup(origPassword: String) = viewModelScope.launch(Dispatchers.IO) {
        restoreState = RestoreState.RESTORING

        val zipInputStream = backupRepository.openBackupInput(fileUri)
        val metaData = metaData ?: error("meta.json was loaded without success")

        val restoreStrategy = getRestoreStrategy(backupVersion) ?: error("Unknown backup version")
        val result = restoreStrategy.restore(metaData, zipInputStream, origPassword)
        zipInputStream.close()

        restoreState = if (result.errors > 0) {
            RestoreState.FINISHED_WITH_ERRORS
        } else {
            RestoreState.FINISHED
        }

    }
}
