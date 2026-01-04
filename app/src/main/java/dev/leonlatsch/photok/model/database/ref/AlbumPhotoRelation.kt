


package dev.leonlatsch.photok.model.database.ref

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.Relation
import dev.leonlatsch.photok.model.database.entity.AlbumTable
import dev.leonlatsch.photok.model.database.entity.Photo

private const val ALBUM_UUID = "album_uuid"
private const val PHOTO_UUID = "photo_uuid"

@Entity(
    primaryKeys = [ALBUM_UUID, PHOTO_UUID],
    tableName = AlbumPhotoCrossRefTable.TABLE_NAME,
)
data class AlbumPhotoCrossRefTable(
    @ColumnInfo(name = ALBUM_UUID) val albumUUID: String,
    @ColumnInfo(name = PHOTO_UUID, index = true) val photoUUID: String,

    @ColumnInfo(name = COL_LINKED_AT)
    val linkedAt: Long
) {
    companion object {
        const val TABLE_NAME = "album_photos_cross_ref"
        const val COL_LINKED_AT = "linked_at"
    }
}

data class AlbumWithPhotos(
    @Embedded val album: AlbumTable,
    @Relation(
        parentColumn = ALBUM_UUID,
        entityColumn = PHOTO_UUID,
        associateBy = Junction(AlbumPhotoCrossRefTable::class)
    )
    val photos: List<Photo>
)

package dev.leonlatsch.photok.model.database.ref

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.Relation
import dev.leonlatsch.photok.model.database.entity.AlbumTable
import dev.leonlatsch.photok.model.database.entity.Photo

private const val ALBUM_UUID = "album_uuid"
private const val PHOTO_UUID = "photo_uuid"

@Entity(
    primaryKeys = [ALBUM_UUID, PHOTO_UUID],
    tableName = AlbumPhotoCrossRefTable.TABLE_NAME,
)
data class AlbumPhotoCrossRefTable(
    @ColumnInfo(name = ALBUM_UUID) val albumUUID: String,
    @ColumnInfo(name = PHOTO_UUID, index = true) val photoUUID: String,

    @ColumnInfo(name = COL_LINKED_AT)
    val linkedAt: Long
) {
    companion object {
        const val TABLE_NAME = "album_photos_cross_ref"
        const val COL_LINKED_AT = "linked_at"
    }
}

data class AlbumWithPhotos(
    @Embedded val album: AlbumTable,
    @Relation(
        parentColumn = ALBUM_UUID,
        entityColumn = PHOTO_UUID,
        associateBy = Junction(AlbumPhotoCrossRefTable::class)
    )
    val photos: List<Photo>
)