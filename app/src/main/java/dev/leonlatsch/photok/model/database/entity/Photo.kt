package dev.leonlatsch.photok.model.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import java.util.UUID

/**
 * Entity describing a Photo.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
// TODO: Add a domain model for photos
@Entity(tableName = Photo.TABLE_NAME)
data class Photo(
    @Expose
    @ColumnInfo(name = COL_FILENAME)
    val fileName: String,

    @ColumnInfo(name = COL_IMPORTED_AT)
    var importedAt: Long,

    @Expose val type: PhotoType,
    @Expose
    @ColumnInfo(name = COL_SIZE)
    var size: Long = 0L,

    @ColumnInfo(name = COL_LAST_MODIFIED, defaultValue = "NULL")
    @Expose
    var lastModified: Long?,

    @Expose
    @PrimaryKey
    @ColumnInfo(name = "photo_uuid")
    val uuid: String = UUID.randomUUID().toString(),
) {

    val internalFileName: String
        get() = internalFileName(uuid)

    val internalThumbnailFileName: String
        get() = internalThumbnailFileName(uuid)

    val internalVideoPreviewFileName: String
        get() = internalVideoPreviewFileName(uuid)

    companion object {
        const val COL_FILENAME = "fileName"
        const val COL_IMPORTED_AT = "importedAt"
        const val COL_LAST_MODIFIED = "lastModified"
        const val DATE_TAKEN = "dateTaken"
        const val COL_SIZE = "size"
        const val TABLE_NAME = "photo"

    }
}