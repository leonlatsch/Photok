package dev.leonlatsch.photok.model.database.entity

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photo")
data class Photo(
    @PrimaryKey(autoGenerate = false) val id: String,
    var data: Bitmap,
    val importedAt: Long,
    val type: PhotoType
)