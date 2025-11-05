/*
 *   Copyright 2020-2022 Leon Latsch
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package dev.leonlatsch.photok.model.database

import androidx.room.TypeConverter
import dev.leonlatsch.photok.gallery.sort.domain.Sort
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
    fun fromSortOrder(order: Sort.Order) = order.sql

    @TypeConverter
    fun toSortField(value: Int): Sort.Field = Sort.Field.fromValue(value)

    @TypeConverter
    fun fromSortField(field: Sort.Field) = field.columnName
}