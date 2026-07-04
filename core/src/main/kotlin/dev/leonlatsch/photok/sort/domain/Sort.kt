/*
 *   Copyright 2020–2026 Leon Latsch
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

package dev.leonlatsch.photok.sort.domain

import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.ref.AlbumPhotoCrossRefTable

data class Sort(
    val field: Field,
    val order: Order,
) {
    enum class Order(val value: Int, val sql: String) {
        Asc(0, "ASC"),
        Desc(1, "DESC");

        companion object {
            fun fromValue(value: Int) = when (value) {
                Asc.value -> Asc
                Desc.value -> Desc
                else -> error("Invalid value $value")
            }
        }
    }

    enum class Field(val value: Int, val columnName: String) {
        ImportDate(0, Photo.COL_IMPORTED_AT),
        FileName(1, Photo.COL_FILENAME),
        Size(2, Photo.COL_SIZE),
        LinkedAt(3, AlbumPhotoCrossRefTable.COL_LINKED_AT),
        LastModified(4, Photo.COL_LAST_MODIFIED);

        companion object {
            fun fromValue(value: Int) = when (value) {
                ImportDate.value -> ImportDate
                FileName.value -> FileName
                Size.value -> Size
                LinkedAt.value -> LinkedAt
                LastModified.value -> LastModified
                else -> error("Invalid value $value")
            }
        }
    }
}
