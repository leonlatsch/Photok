package dev.leonlatsch.photok.gallery.ui

import dev.leonlatsch.photok.sort.domain.Sort
import android.net.Uri
import dev.leonlatsch.photok.gallery.components.PhotoTile
import dev.leonlatsch.photok.model.database.entity.Photo
import javax.inject.Inject

class GalleryUiStateFactory @Inject constructor() {
    fun create(
        photos: List<Photo>,
        showAlbumSelectionDialog: Boolean,
        sort: Sort,
    ): GalleryUiState {
        return if (photos.isEmpty()) {
            GalleryUiState.Empty
        } else {
            GalleryUiState.Content(
                photos = photos.map {
                    PhotoTile(
                        fileName = it.fileName,
                        type = it.type,
                        uuid = it.uuid
                    )
                },
                showAlbumSelectionDialog = showAlbumSelectionDialog,
                sort = sort,
            )
        }
    }
}