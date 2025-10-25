/*
 *   Copyright 2020-2024 Leon Latsch
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

package dev.leonlatsch.photok.model.database.dao

import dev.leonlatsch.photok.model.database.entity.Photo


data class Sort(
    val field: Field,
    val order: Order,
) {
    enum class Order(val label: String) {
        ASK("Ascending"),
        DESC("Descending"),
    }

    enum class Field(val columnName: String, val label: String) {
        IMPORT(Photo.COL_IMPORTED_AT, "Import date"),
        FILE_NAME(Photo.COL_FILENAME, "Filename"),
        SIZE(Photo.COL_SIZE, "Size"),

    }
}