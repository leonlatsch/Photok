package dev.leonlatsch.photok.model.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photo")
data class Photo(
    @PrimaryKey(autoGenerate = false) val id: String,
    val uri: String,
    val importedAt: Long,
)