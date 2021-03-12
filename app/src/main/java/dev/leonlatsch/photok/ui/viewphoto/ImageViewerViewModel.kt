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

package dev.leonlatsch.photok.ui.viewphoto

import android.app.Application
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.ui.components.bindings.ObservableViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for loading the full size photo to [ViewPhotoActivity].
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class ImageViewerViewModel @Inject constructor(
    private val app: Application,
    val photoRepository: PhotoRepository
) : ObservableViewModel(app) {

    var ids = listOf<Int>()

    @get:Bindable
    var currentPhoto: Photo? = null
        set(value) {
            field = value
            notifyChange(BR.currentPhoto, value)
        }

    /**
     * Load all photo Ids.
     * Save them in viewModel and pass them to [onFinished].
     */
    fun preloadData(onFinished: (List<Int>) -> Unit) = viewModelScope.launch {
        if (ids.isEmpty()) {
            ids = photoRepository.getAllIds()
        }
        onFinished(ids)
    }

    /**
     * Loads a photo. Gets called after onViewCreated
     */
    fun updateDetails(position: Int) = viewModelScope.launch {
        val photo = photoRepository.get(ids[position])
        currentPhoto = photo
    }

    /**
     * Deletes a single photo. Called after verification.
     *
     * @param onSuccess Block called on success
     * @param onError Block called on error
     */
    fun deletePhoto(onSuccess: () -> Unit, onError: () -> Unit) =
        viewModelScope.launch(Dispatchers.IO) {
            currentPhoto ?: return@launch
            currentPhoto!!.id ?: return@launch

            val success = photoRepository.safeDeletePhoto(app, currentPhoto!!)
            if (success) onSuccess() else onError()
        }

    /**
     * Exports a single photo. Called after verification.
     *
     * @param onSuccess Block called on success
     * @param onError Block called on error
     */
    fun exportPhoto(onSuccess: () -> Unit, onError: () -> Unit) =
        viewModelScope.launch(Dispatchers.IO) {
            currentPhoto ?: return@launch
            currentPhoto!!.id ?: return@launch

            val success = photoRepository.exportPhoto(app, currentPhoto!!)
            if (success) onSuccess() else onError()
        }
}