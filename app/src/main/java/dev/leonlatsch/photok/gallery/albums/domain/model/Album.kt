


package dev.leonlatsch.photok.gallery.albums.domain.model

import dev.leonlatsch.photok.model.database.entity.Photo
import java.util.UUID

data class Album(
    val uuid: String = UUID.randomUUID().toString(),
    val name: String,
    val modifiedAt: Long,
    val files: List<Photo>,
) {
    companion object {
        val Placeholder = Album(name = "", modifiedAt = 0, files = emptyList())
    }
}

package dev.leonlatsch.photok.gallery.albums.domain.model

import dev.leonlatsch.photok.model.database.entity.Photo
import java.util.UUID

data class Album(
    val uuid: String = UUID.randomUUID().toString(),
    val name: String,
    val modifiedAt: Long,
    val files: List<Photo>,
) {
    companion object {
        val Placeholder = Album(name = "", modifiedAt = 0, files = emptyList())
    }
}