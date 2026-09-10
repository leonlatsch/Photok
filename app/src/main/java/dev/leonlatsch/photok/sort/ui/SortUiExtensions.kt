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

package dev.leonlatsch.photok.sort.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.sort.domain.Sort

val Sort.Order.icon: Int @DrawableRes get() = when (this) {
    Sort.Order.Asc -> R.drawable.ic_double_arrow_up
    Sort.Order.Desc -> R.drawable.ic_double_arrow_down
}

val Sort.Order.label: Int @StringRes get() = when (this) {
    Sort.Order.Asc -> R.string.sorting_order_asc_label
    Sort.Order.Desc -> R.string.sorting_order_desc_label
}

val Sort.Field.icon: Int @DrawableRes get() = when (this) {
    Sort.Field.ImportDate -> R.drawable.ic_calendar_today
    Sort.Field.FileName -> R.drawable.ic_abc
    Sort.Field.Size -> R.drawable.ic_photo_size
    Sort.Field.LinkedAt -> R.drawable.ic_calendar_today
    Sort.Field.LastModified -> R.drawable.ic_edit
}

val Sort.Field.label: Int @StringRes get() = when (this) {
    Sort.Field.ImportDate -> R.string.sorting_field_import_date_label
    Sort.Field.FileName -> R.string.sorting_field_filename_label
    Sort.Field.Size -> R.string.sorting_field_size_label
    Sort.Field.LinkedAt -> R.string.sorting_field_added_to_album_label
    Sort.Field.LastModified -> R.string.sorting_field_last_modified
}
