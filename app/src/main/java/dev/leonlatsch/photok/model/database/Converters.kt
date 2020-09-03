package dev.leonlatsch.photok.model.database

import androidx.room.TypeConverter
import dev.leonlatsch.photok.model.database.entity.PhotoType

/**
 * TypeConverters for Room
 *
 * @since 1.0.0
 */
class Converters {

    /**
     * Convert [PhotoType] to [Int].
     */
    @TypeConverter
    fun fromPhotoType(photoType: PhotoType): Int = photoType.value

    /**
     * Convert [Int] to [PhotoType].
     */
    @TypeConverter
    fun toPhotoType(photoType: Int): PhotoType = PhotoType.fromValue(photoType)
}