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
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.backup.data.BackupMetaData
import dev.leonlatsch.photok.backup.domain.GetBackupRestoreStrategyUseCase
import dev.leonlatsch.photok.other.extensions.empty
import dev.leonlatsch.photok.other.extensions.lazyClose
import dev.leonlatsch.photok.other.getFileName
import dev.leonlatsch.photok.other.getFileSize
import dev.leonlatsch.photok.uicomponnets.bindings.ObservableViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.BufferedInputStream
import java.io.IOException
import java.util.zip.ZipInputStream
import javax.inject.Inject

/**
 * ViewModel for loading and validating a backup file.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class RestoreBackupViewModel @Inject constructor(
    private val app: Application,
    private val getRestoreStrategy: GetBackupRestoreStrategyUseCase,
    private val gson: Gson,
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
        var photoFiles = 0
        fileUri = uri

        createStream(fileUri)?.use { stream ->
            var ze = stream.nextEntry
            while (ze != null) {
                if (ze.name == BackupMetaData.FILE_NAME) {
                    val bytes = stream.readBytes()
                    val string = String(bytes)
                    metaData = gson.fromJson(string, BackupMetaData::class.java)
                    backupVersion = metaData.getBackupVersion()


                } else if (ze.name.endsWith(".photok")) {
                    photoFiles++
                }

                ze = stream.nextEntry
            }
        }

        zipFileName = getFileName(app.contentResolver, fileUri) ?: String.empty

        // Validate backup
        if (metaData?.photos?.size == photoFiles
            && BackupMetaData.VALID_BACKUP_VERSIONS.contains(backupVersion)
        ) {
            backupSize = getFileSize(app.contentResolver, fileUri)
            restoreState = RestoreState.FILE_VALID
        }

        if (restoreState == RestoreState.INITIALIZE) {
            restoreState = RestoreState.FILE_INVALID
        }
    }

    /**
     * Restore the validated backup with the original password.
     */
    fun restoreBackup(origPassword: String) = viewModelScope.launch(Dispatchers.IO) {
        restoreState = RestoreState.RESTORING

        val zipInputStream = createStream(fileUri)
        val metaData = metaData ?: error("meta.json was loaded without success")

        val restoreStrategy = getRestoreStrategy(backupVersion)
        restoreStrategy.restore(metaData, zipInputStream, origPassword)

        zipInputStream.lazyClose()
        restoreState = RestoreState.FINISHED
    }

    private fun createStream(uri: Uri): ZipInputStream {
        val inputStream = try {
            app.contentResolver.openInputStream(uri)
        } catch (e: IOException) {
            Timber.d("Error opening backup at: $uri")
            null
        }
        return if (inputStream != null) {
            ZipInputStream(BufferedInputStream(inputStream))
        } else {
            error("Could not open zip file at $uri")
        }
    }
}

private fun BackupMetaData?.getBackupVersion(): Int {
    this?.let {
        return if (it.backupVersion == 0) { // Treat legacy version 0 as 1
            1
        } else {
            it.backupVersion
        }
    }

    return -1
}