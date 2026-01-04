


package dev.leonlatsch.photok.model.database

import androidx.room.TypeConverter
import dev.leonlatsch.photok.sort.domain.Sort
import dev.leonlatsch.photok.model.database.entity.PhotoType

/**
 * TypeConverters for Room
 *
 * @since 1.0.0
 * @author Leon Latsch
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


    @TypeConverter
    fun toSortOrder(value: Int): Sort.Order = Sort.Order.fromValue(value)

    @TypeConverter
    fun fromSortOrder(order: Sort.Order) = order.value

    @TypeConverter
    fun toSortField(value: Int): Sort.Field = Sort.Field.fromValue(value)

    @TypeConverter
    fun fromSortField(field: Sort.Field): Int = field.value
}

package dev.leonlatsch.photok.model.database

import androidx.room.TypeConverter
import dev.leonlatsch.photok.sort.domain.Sort
import dev.leonlatsch.photok.model.database.entity.PhotoType

/**
 * TypeConverters for Room
 *
 * @since 1.0.0
 * @author Leon Latsch
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


    @TypeConverter
    fun toSortOrder(value: Int): Sort.Order = Sort.Order.fromValue(value)

    @TypeConverter
    fun fromSortOrder(order: Sort.Order) = order.value

    @TypeConverter
    fun toSortField(value: Int): Sort.Field = Sort.Field.fromValue(value)

    @TypeConverter
    fun fromSortField(field: Sort.Field): Int = field.value
}