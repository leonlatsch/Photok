package dev.leonlatsch.photok.model.database

import androidx.room.TypeConverter
import dev.leonlatsch.photok.model.database.entity.PhotoType

class Converters {

    @TypeConverter
    fun fromPhotoType(photoType: PhotoType): Int = photoType.value

    @TypeConverter
    fun toPhotoType(photoType: Int): PhotoType = PhotoType.fromValue(photoType)
}