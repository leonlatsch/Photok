/*
 *   Copyright 2020 Leon Latsch
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
import androidx.hilt.lifecycle.ViewModelInject
import com.google.gson.Gson
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.other.emptyString
import dev.leonlatsch.photok.other.getFileName
import dev.leonlatsch.photok.ui.components.bindings.ObservableViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.util.zip.ZipInputStream

/**
 * ViewModel for loading and validating a backup file.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class ValidateBackupViewModel @ViewModelInject constructor(
    private val app: Application
) : ObservableViewModel(app) {

    @Bindable
    var restoreState: RestoreState = RestoreState.INITIALIZE
        set(value) {
            field = value
            notifyChange(BR.restoreState, value)
        }

    /**
     * [BackupDetails] holding meta data of the loaded backup.
     */
    @get:Bindable
    var metaData: BackupDetails? = null
        set(value) {
            field = value
            notifyChange(BR.metaData, value)
        }

    /**
     * File name of the zip.
     */
    @get:Bindable
    var zipFileName: String? = emptyString()
        set(value) {
            field = value
            notifyChange(BR.zipFileName, value)
        }

    /**
     * Load and Validate a backup file. Fill [metaData].
     */
    fun loadAndValidateBackup(uri: Uri) = GlobalScope.launch(Dispatchers.IO) {
        var photoFiles = 0

        // Load backup
        createStream(uri)?.use { stream ->
            zipFileName = getFileName(app.contentResolver, uri)
            var ze = stream.nextEntry
            while (ze != null) {
                if (ze.name == BackupDetails.FILE_NAME) {
                    val bytes = stream.readBytes()
                    val string = String(bytes)
                    metaData = Gson().fromJson(string, BackupDetails::class.java)

                } else {
                    photoFiles++
                }

                ze = stream.nextEntry
            }
        }

        // Validate backup
        if (metaData?.photos?.size == photoFiles) {
            restoreState = RestoreState.FILE_VALID
        }

        if (restoreState == RestoreState.INITIALIZE) {
            restoreState = RestoreState.FILE_INVALID
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
            ZipInputStream(inputStream)
        } else {
            null
        }
    }
}