


package dev.leonlatsch.photok.imageviewer.ui

import android.app.Application
import android.net.Uri
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.other.onMain
import dev.leonlatsch.photok.uicomponnets.bindings.ObservableViewModel
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
    app: Application,
    val photoRepository: PhotoRepository,
    val albumRepository: AlbumRepository,
) : ObservableViewModel(app) {

    var photos = listOf<Photo>()

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
    fun preloadData(
        albumUUID: String,
        onFinished: (List<Photo>) -> Unit
    ) = viewModelScope.launch {
        if (photos.isNotEmpty()) {
            onFinished(photos)
            return@launch
        }

        photos = if (albumUUID.isEmpty()) {
            photoRepository.findAllPhotosByImportDateDesc()
        } else {
            albumRepository.getPhotosForAlbum(albumUUID)
        }

        onFinished(photos)
    }

    /**
     * Loads a photo. Gets called after onViewCreated
     */
    fun updateDetails(position: Int) = viewModelScope.launch {
        currentPhoto = photos[position]
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

            photoRepository.safeDeletePhoto(currentPhoto!!).let {
                onMain {
                    if (it) onSuccess() else onError()
                }
            }
        }

    /**
     * Exports a single photo. Called after verification.
     *
     * @param onSuccess Block called on success
     * @param onError Block called on error
     */
    fun exportPhoto(target: Uri, onSuccess: () -> Unit, onError: () -> Unit) =
        viewModelScope.launch(Dispatchers.IO) {
            currentPhoto?.let { safeCurrentPhoto ->
                photoRepository.exportPhoto(safeCurrentPhoto, target).let { success ->
                    onMain {
                        if (success) onSuccess() else onError()
                    }
                }
            }

        }
}

package dev.leonlatsch.photok.imageviewer.ui

import android.app.Application
import android.net.Uri
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.other.onMain
import dev.leonlatsch.photok.uicomponnets.bindings.ObservableViewModel
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
    app: Application,
    val photoRepository: PhotoRepository,
    val albumRepository: AlbumRepository,
) : ObservableViewModel(app) {

    var photos = listOf<Photo>()

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
    fun preloadData(
        albumUUID: String,
        onFinished: (List<Photo>) -> Unit
    ) = viewModelScope.launch {
        if (photos.isNotEmpty()) {
            onFinished(photos)
            return@launch
        }

        photos = if (albumUUID.isEmpty()) {
            photoRepository.findAllPhotosByImportDateDesc()
        } else {
            albumRepository.getPhotosForAlbum(albumUUID)
        }

        onFinished(photos)
    }

    /**
     * Loads a photo. Gets called after onViewCreated
     */
    fun updateDetails(position: Int) = viewModelScope.launch {
        currentPhoto = photos[position]
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

            photoRepository.safeDeletePhoto(currentPhoto!!).let {
                onMain {
                    if (it) onSuccess() else onError()
                }
            }
        }

    /**
     * Exports a single photo. Called after verification.
     *
     * @param onSuccess Block called on success
     * @param onError Block called on error
     */
    fun exportPhoto(target: Uri, onSuccess: () -> Unit, onError: () -> Unit) =
        viewModelScope.launch(Dispatchers.IO) {
            currentPhoto?.let { safeCurrentPhoto ->
                photoRepository.exportPhoto(safeCurrentPhoto, target).let { success ->
                    onMain {
                        if (success) onSuccess() else onError()
                    }
                }
            }

        }
}