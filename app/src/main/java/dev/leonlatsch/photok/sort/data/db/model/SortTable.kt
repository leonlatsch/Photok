package dev.leonlatsch.photok.sort.data.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import dev.leonlatsch.photok.model.database.entity.AlbumTable
import dev.leonlatsch.photok.sort.domain.Sort

@Entity(
    tableName = SortTable.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = AlbumTable::class,
            parentColumns = arrayOf(AlbumTable.ALBUM_UUID),
            childColumns = arrayOf(AlbumTable.ALBUM_UUID),
            onDelete = ForeignKey.CASCADE,
        ),
    ]
)
data class SortTable(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name = AlbumTable.ALBUM_UUID, index = true)
    val albumUuid: String? = null,
    val field: Sort.Field,
    val order: Sort.Order,
) {
    companion object {
        const val TABLE_NAME = "sort"
    }
}