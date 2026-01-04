


package dev.leonlatsch.photok.gallery.ui.menu

import android.app.Application
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.uicomponnets.base.processdialogs.BaseProcessViewModel
import javax.inject.Inject

/**
 * ViewModel for deleting multiple photos.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class DeleteViewModel @Inject constructor(
    app: Application,
    private val photoRepository: PhotoRepository
) : BaseProcessViewModel<Photo>(app) {

    override suspend fun processItem(item: Photo) {
        if (item.uuid.isEmpty()) {
            failuresOccurred = true
            return
        }

        val success = photoRepository.safeDeletePhoto(item)
        if (!success) {
            failuresOccurred = true
        }
    }

}

package dev.leonlatsch.photok.gallery.ui.menu

import android.app.Application
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.uicomponnets.base.processdialogs.BaseProcessViewModel
import javax.inject.Inject

/**
 * ViewModel for deleting multiple photos.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class DeleteViewModel @Inject constructor(
    app: Application,
    private val photoRepository: PhotoRepository
) : BaseProcessViewModel<Photo>(app) {

    override suspend fun processItem(item: Photo) {
        if (item.uuid.isEmpty()) {
            failuresOccurred = true
            return
        }

        val success = photoRepository.safeDeletePhoto(item)
        if (!success) {
            failuresOccurred = true
        }
    }

}