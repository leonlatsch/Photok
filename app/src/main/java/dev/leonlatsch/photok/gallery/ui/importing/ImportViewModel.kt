


package dev.leonlatsch.photok.gallery.ui.importing

import android.app.Application
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.model.repositories.ImportSource
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.uicomponnets.base.processdialogs.BaseProcessViewModel
import javax.inject.Inject

/**
 * View model to handle importing photos.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class ImportViewModel @Inject constructor(
    app: Application,
    private val photoRepository: PhotoRepository,
    private val albumRepository: AlbumRepository,
    private val sharedUrisStore: SharedUrisStore,
) : BaseProcessViewModel<Uri>(app) {

    var albumUUID: String? = null
    var importSource = ImportSource.InApp

    override suspend fun processItem(item: Uri) {
        val photoUUID = photoRepository.safeImportPhoto(
            sourceUri = item,
            importSource = importSource,
        )
        if (photoUUID.isEmpty()) {
            failuresOccurred = true
            return
        }

        albumUUID?.let {
            albumRepository.link(listOf(photoUUID), it)
        }
    }

    override suspend fun postProcess() {
        super.postProcess()

        sharedUrisStore.reset()
    }
}

package dev.leonlatsch.photok.gallery.ui.importing

import android.app.Application
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.model.repositories.ImportSource
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.uicomponnets.base.processdialogs.BaseProcessViewModel
import javax.inject.Inject

/**
 * View model to handle importing photos.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class ImportViewModel @Inject constructor(
    app: Application,
    private val photoRepository: PhotoRepository,
    private val albumRepository: AlbumRepository,
    private val sharedUrisStore: SharedUrisStore,
) : BaseProcessViewModel<Uri>(app) {

    var albumUUID: String? = null
    var importSource = ImportSource.InApp

    override suspend fun processItem(item: Uri) {
        val photoUUID = photoRepository.safeImportPhoto(
            sourceUri = item,
            importSource = importSource,
        )
        if (photoUUID.isEmpty()) {
            failuresOccurred = true
            return
        }

        albumUUID?.let {
            albumRepository.link(listOf(photoUUID), it)
        }
    }

    override suspend fun postProcess() {
        super.postProcess()

        sharedUrisStore.reset()
    }
}