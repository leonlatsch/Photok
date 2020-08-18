package dev.leonlatsch.photok.model.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photo")
data class Photo(
    val uri: String,
    val importedAt: Long,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)