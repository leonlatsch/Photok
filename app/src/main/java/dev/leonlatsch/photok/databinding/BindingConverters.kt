/*
 *   Copyright 2020 Leon Latsch
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

package dev.leonlatsch.photok.databinding

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

object BindingConverters {

    private val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
    private val decimalFormat = DecimalFormat("0")

    private const val GB_SUFFIX = " GB"
    private const val MB_SUFFIX = " MB"
    private const val KB_SUFFIX = " KB"
    private const val BYTES_SUFFIX = " Bytes"

    fun millisToFormattedDateConverter(millis: Long): String? {
        val date = Date(millis)
        return dateFormat.format(date)
    }

    fun formatByteSizeConverter(bytes: Int): String {
        val kiloBytes = bytes / 1024
        val megaBytes = kiloBytes / 1024
        val gigaBytes = megaBytes / 1024

        return when {
            gigaBytes >= 1 -> {
                decimalFormat.format(gigaBytes) + GB_SUFFIX
            }
            megaBytes >= 1 -> {
                decimalFormat.format(megaBytes) + MB_SUFFIX
            }
            kiloBytes >= 1 -> {
                decimalFormat.format(kiloBytes) + KB_SUFFIX
            }
            else -> {
                decimalFormat.format(bytes) + BYTES_SUFFIX
            }
        }
    }
}