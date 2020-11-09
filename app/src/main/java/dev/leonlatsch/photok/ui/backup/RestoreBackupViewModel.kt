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
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.ui.components.ObservableViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.zip.ZipInputStream

class RestoreBackupViewModel @ViewModelInject constructor(
    private val app: Application
) : ObservableViewModel(app) {

    val restoreState: MutableLiveData<RestoreState> = MutableLiveData(RestoreState.INITIALIZE)

    @get:Bindable
    var metaData: BackupDetails? = null
        set(value) {
            field = value
            notifyChange(BR.metaData)
        }

    fun loadFile(uri: Uri) = GlobalScope.launch(Dispatchers.IO) {
        val inputStream = createStream(uri)
        var photoFiles = 0

        while (true) {
            val ze = inputStream.nextEntry
            ze ?: break

            if (ze.name == BackupDetails.FILE_NAME) {
                val bytes = inputStream.readBytes()
                val string = String(bytes)
                metaData = Gson().fromJson(string, BackupDetails::class.java)

            } else {
                photoFiles++
            }
        }
        inputStream.close()

        if (metaData?.photos?.size == photoFiles) {
            restoreState.postValue(RestoreState.FILE_VALID)
            delay(1)
        }

        if (restoreState.value == RestoreState.INITIALIZE) {
            restoreState.postValue(RestoreState.FILE_INVALID)
        }

    }

    private fun createStream(uri: Uri): ZipInputStream {
        val inputStream = app.contentResolver.openInputStream(uri)
        return ZipInputStream(inputStream)
    }
}