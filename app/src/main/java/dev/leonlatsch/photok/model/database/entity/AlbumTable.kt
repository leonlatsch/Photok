


package dev.leonlatsch.photok.model.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = AlbumTable.TABLE_NAME)
data class AlbumTable(
    val name: String,
    @PrimaryKey
    @ColumnInfo(name = ALBUM_UUID)
    val uuid: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "modified_at", defaultValue = "0")
    val modifiedAt: Long,
) {
    companion object {
        const val TABLE_NAME = "album"
        const val ALBUM_UUID = "album_uuid"
    }
}

package dev.leonlatsch.photok.model.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = AlbumTable.TABLE_NAME)
data class AlbumTable(
    val name: String,
    @PrimaryKey
    @ColumnInfo(name = ALBUM_UUID)
    val uuid: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "modified_at", defaultValue = "0")
    val modifiedAt: Long,
) {
    companion object {
        const val TABLE_NAME = "album"
        const val ALBUM_UUID = "album_uuid"
    }
}