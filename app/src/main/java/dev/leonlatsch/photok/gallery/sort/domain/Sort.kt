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

package dev.leonlatsch.photok.gallery.sort.domain

import androidx.annotation.DrawableRes
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.model.database.entity.Photo

data class Sort(
    val field: Field,
    val order: Order,
) {
    enum class Order(@DrawableRes val icon: Int, val label: String, val sql: String) {
        Asc(R.drawable.ic_double_arrow_up, "Ascending", "ASC"),
        Desc(R.drawable.ic_double_arrow_down, "Descending", "DESC"),
    }

    enum class Field(val columnName: String, @DrawableRes val icon: Int, val label: String) {
        ImportDate(Photo.Companion.COL_IMPORTED_AT, R.drawable.ic_calendar_today, "Import date"),
        FileName(Photo.Companion.COL_FILENAME, R.drawable.ic_abc, "Filename"),
        Size(Photo.Companion.COL_SIZE, R.drawable.ic_photo_size, "Size"),

    }

    companion object {
        val Default = Sort(
            field = Field.ImportDate,
            order = Order.Desc,
        )
    }
}