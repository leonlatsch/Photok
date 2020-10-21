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

package dev.leonlatsch.photok.ui.process

import android.app.Application
import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.other.getFileName
import dev.leonlatsch.photok.ui.process.base.BaseProcessViewModel
import java.util.*

/**
 * View model to handle importing photos.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class ImportViewModel @ViewModelInject constructor(
    private val app: Application,
    private val photoRepository: PhotoRepository
) : BaseProcessViewModel<Uri>() {

    override suspend fun processItem(item: Uri) {
        val fileName = getFileName(app.contentResolver, item) ?: UUID.randomUUID().toString()

        val type = when (app.contentResolver.getType(item)) {
            "image/png" -> PhotoType.PNG
            "image/jpeg" -> PhotoType.JPEG
            "image/gif" -> PhotoType.GIF
            else -> PhotoType.UNDEFINED
        }
        if (type == PhotoType.UNDEFINED) {
            failuresOccurred = true
            return
        }

        val bytes = photoRepository.readPhotoFromExternal(app.contentResolver, item)
        if (bytes == null) { // Cloud not read file
            failuresOccurred = true
            return
        }

        val photo = Photo(fileName, System.currentTimeMillis(), type)
        val success = photoRepository.safeCreatePhoto(app, photo, bytes)
        if (!success) {
            failuresOccurred = true
        }
    }
}