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

package dev.leonlatsch.photok.ui.importing

import android.app.Application
import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.leonlatsch.photok.base.NumberLiveData
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.other.getFileName
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class ImportViewModel @ViewModelInject constructor(
    private val app: Application,
    private val photoRepository: PhotoRepository
) : ViewModel() {

    var importState: MutableLiveData<ImportState> = MutableLiveData()
    var importProgress: MutableLiveData<ImportProgress> = MutableLiveData()
    var failed: NumberLiveData = NumberLiveData()

    private var aborted = false

    fun runImport(uris: List<Uri>) = viewModelScope.launch {
        var current = 1
        importState.postValue(ImportState.IMPORTING)
        importProgress.value?.update(0, uris.size)

        for (image in uris) {
            if (aborted) {
                importState.postValue(ImportState.ABORTED)
                return@launch
            }
            // Load Bytes
            import(image)
            importProgress.value?.update(current, uris.size)
            current++
        }

        if (failed.value!! > 0) {
            Timber.d("$failed photos failed to import")
        }
        importState.postValue(ImportState.FINISHED)
    }

    private suspend fun import(imageUri: Uri) {
        val fileName = getFileName(app.contentResolver, imageUri) ?: UUID.randomUUID().toString()

        val type = when (app.contentResolver.getType(imageUri)) {
            "image/png" -> PhotoType.PNG
            "image/jpeg" -> PhotoType.JPEG
            "image/gif" -> PhotoType.GIF
            else -> PhotoType.UNDEFINED
        }
        if (type == PhotoType.UNDEFINED) {
            failed.increment()
            return
        }

        val bytes = photoRepository.readPhotoFromExternal(app.contentResolver, imageUri)
        if (bytes == null) {
            failed.increment()
            return
        }

        val photo = Photo(fileName, System.currentTimeMillis(), type)
        val id = photoRepository.insert(photo)
        photoRepository.writePhotoData(app, id, bytes)
    }

    fun abortImport() {
        aborted = true
    }
}