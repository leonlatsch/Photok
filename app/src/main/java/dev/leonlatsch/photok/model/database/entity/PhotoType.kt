/*
 *   Copyright 2020-2021 Leon Latsch
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

package dev.leonlatsch.photok.model.database.entity

import dev.leonlatsch.photok.other.empty

/**
 * Enum for [Photo.type].
 * Internal value is an [Int].
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
enum class PhotoType(val value: Int, val mimeType: String) {
    UNDEFINED(0, String.empty),
    PNG(1, "image/png"),
    JPEG(2, "image/jpeg"),
    GIF(3, "image/gif"),
    MP4(4, "video/gif"),
    MPEG(5, "video/mpeg");

    val isVideo: Boolean
        get() = when (value) {
            4, 5 -> true
            else -> false
        }

    companion object {
        /**
         * Create a [PhotoType] from its Int value.
         * Used in converters.
         */
        fun fromValue(value: Int) = values().first { it.value == value }
    }

}