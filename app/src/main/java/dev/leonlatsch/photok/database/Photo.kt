package dev.leonlatsch.photok.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photo")
data class Photo(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val uri: String,
    val importedAt: Long
)