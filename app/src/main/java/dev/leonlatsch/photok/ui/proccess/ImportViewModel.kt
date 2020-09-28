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

package dev.leonlatsch.photok.ui.proccess

import android.app.Application
import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.other.getFileName
import dev.leonlatsch.photok.ui.proccess.base.BaseProcessViewModel
import dev.leonlatsch.photok.ui.proccess.base.ProcessState
import kotlinx.coroutines.launch
import java.util.*

class ImportViewModel @ViewModelInject constructor(
    private val app: Application,
    private val photoRepository: PhotoRepository
) : BaseProcessViewModel(){

    lateinit var uris: List<Uri>

    override fun process() = viewModelScope.launch {
        var current = 1
        processState.postValue(ProcessState.PROCESSING)
        progress.value?.update(0, uris.size)

        for (image in uris) {
            if (processState.value == ProcessState.ABORTED) {
                return@launch
            }

            // Import image
            import(image)
            progress.value?.update(current, uris.size)
            current++
        }

        processState.postValue(ProcessState.FINISHED)
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
            return
        }

        val bytes = photoRepository.readPhotoFromExternal(app.contentResolver, imageUri)
        if (bytes == null) {
            failuresOccurred = false
            return
        }

        val photo = Photo(fileName, System.currentTimeMillis(), type)
        val id = photoRepository.insert(photo)
        photoRepository.writePhotoData(app, id, bytes)
    }

    override fun cancel() {
        processState.postValue(ProcessState.ABORTED)
    }
}