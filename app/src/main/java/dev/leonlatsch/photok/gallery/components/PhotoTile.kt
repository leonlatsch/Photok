


package dev.leonlatsch.photok.gallery.components

import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.model.database.entity.internalThumbnailFileName

data class PhotoTile(
    val fileName: String,
    val type: PhotoType,
    val uuid: String,
) {
    val internalThumbnailFileName = internalThumbnailFileName(uuid)
}

package dev.leonlatsch.photok.gallery.components

import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.model.database.entity.internalThumbnailFileName

data class PhotoTile(
    val fileName: String,
    val type: PhotoType,
    val uuid: String,
) {
    val internalThumbnailFileName = internalThumbnailFileName(uuid)
}