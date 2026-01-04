


package dev.leonlatsch.photok.gallery.ui.menu

import android.app.Application
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.uicomponnets.base.processdialogs.BaseProcessViewModel
import javax.inject.Inject

/**
 * ViewModel for exporting multiple photos.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class ExportViewModel @Inject constructor(
    app: Application,
    private val photoRepository: PhotoRepository
) : BaseProcessViewModel<Photo>(app) {

    lateinit var target: Uri

    override suspend fun processItem(item: Photo) {
        val result = photoRepository.exportPhoto(item, target)
        if (!result) {
            failuresOccurred = true
        }
    }
}

package dev.leonlatsch.photok.gallery.ui.menu

import android.app.Application
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.uicomponnets.base.processdialogs.BaseProcessViewModel
import javax.inject.Inject

/**
 * ViewModel for exporting multiple photos.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class ExportViewModel @Inject constructor(
    app: Application,
    private val photoRepository: PhotoRepository
) : BaseProcessViewModel<Photo>(app) {

    lateinit var target: Uri

    override suspend fun processItem(item: Photo) {
        val result = photoRepository.exportPhoto(item, target)
        if (!result) {
            failuresOccurred = true
        }
    }
}