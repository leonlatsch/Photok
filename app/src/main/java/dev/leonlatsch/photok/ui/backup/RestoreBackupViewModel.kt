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

package dev.leonlatsch.photok.ui.backup

import android.app.Application
import android.net.Uri
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.other.*
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.ui.components.bindings.ObservableViewModel
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
    private val photoRepository: PhotoRepository,
    private val encryptionManager: EncryptionManager,
    private val encryptedStorageManager: EncryptedStorageManager
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
    fun loadAndValidateBackup(uri: Uri) = viewModelScope.launchIO {
        var photoFiles = 0
        fileUri = uri

        // Load backup
        createStream(fileUri)?.use { stream ->
            zipFileName = getFileName(app.contentResolver, fileUri) ?: String.empty

            var ze = stream.nextEntry
            while (ze != null) {
                if (ze.name == BackupMetaData.FILE_NAME) {
                    val bytes = stream.readBytes()
                    val string = String(bytes)
                    metaData = Gson().fromJson(string, BackupMetaData::class.java)
                    backupVersion = getVersion()


                } else if (ze.name.endsWith(".photok")) {
                    photoFiles++
                }

                ze = stream.nextEntry
            }
        }

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

    private fun getVersion(): Int {
        metaData?.let {
            return if (it.backupVersion == 0) { // Treat legacy version 0 as 1
                1
            } else {
                it.backupVersion
            }
        }

        return -1
    }

    /**
     * Restore the validated backup with the original password.
     */
    fun restoreBackup(origPassword: String) = viewModelScope.launchIO {
        restoreState = RestoreState.RESTORING

        createStream(fileUri)?.use {
            when (backupVersion) {
                1 -> restoreVersion1(it, origPassword)
                2 -> restoreVersion2(it, origPassword)
            }

            restoreState = RestoreState.FINISHED
        }
    }

    private suspend fun restoreVersion1(stream: ZipInputStream, origPassword: String) {
        TODO()
    }

    private suspend fun restoreVersion2(stream: ZipInputStream, origPassword: String) {
        var ze = stream.nextEntry

        while (ze != null) {
            if (ze.name == BackupMetaData.FILE_NAME) {
                ze = stream.nextEntry
                continue
            }

            val encryptedZipInput =
                encryptionManager.createCipherInputStream(stream, origPassword)
            val internalOutputStream =
                encryptedStorageManager.internalOpenEncryptedFileOutput(app, ze.name)

            if (encryptedZipInput == null || internalOutputStream == null) {
                ze = stream.nextEntry
                continue
            }

            encryptedZipInput.copyTo(internalOutputStream)
            internalOutputStream.lazyClose()

            ze = stream.nextEntry
        }

        metaData?.photos?.forEach {
            val newPhoto = Photo(
                it.fileName,
                System.currentTimeMillis(),
                it.type,
                it.size,
                it.uuid
            )

            photoRepository.insert(newPhoto)
        }
    }

    private fun createStream(uri: Uri): ZipInputStream? {
        val inputStream = try {
            app.contentResolver.openInputStream(uri)
        } catch (e: IOException) {
            Timber.d("Error opening backup at: $uri")
            null
        }
        return if (inputStream != null) {
            ZipInputStream(BufferedInputStream(inputStream))
        } else {
            null
        }
    }
}