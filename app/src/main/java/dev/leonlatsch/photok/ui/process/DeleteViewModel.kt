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
import androidx.hilt.lifecycle.ViewModelInject
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.ui.process.base.BaseProcessViewModel
import dev.leonlatsch.photok.ui.process.base.ProcessState

/**
 * ViewModel for deleting multiple photos.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class DeleteViewModel @ViewModelInject constructor(
    private val app: Application,
    private val photoRepository: PhotoRepository
) : BaseProcessViewModel<Photo>() {

    override suspend fun process() {
        for (photo in items) {
            if (processState.value == ProcessState.ABORTED) {
                return
            }
            currentElement++

            // Delete image
            delete(photo)
            updateProgress()
        }
    }

    private suspend fun delete(photo: Photo) {
        if (photo.id == null) {
            failuresOccurred = true
            return
        }

        val success = photoRepository.safeDeletePhoto(app, photo)
        if (!success) {
            failuresOccurred = true
        }
    }

}