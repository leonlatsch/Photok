package dev.leonlatsch.photok.model.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photo")
data class Photo(
    val fileName: String,
    val importedAt: Long,
    val type: PhotoType,
    @PrimaryKey(autoGenerate = true) val id: Int? = null
)