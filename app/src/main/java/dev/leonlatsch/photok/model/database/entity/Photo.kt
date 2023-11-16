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

package dev.leonlatsch.photok.model.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import java.util.*

/**
 * Entity describing a Photo.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@Entity(tableName = "photo")
data class Photo(
    @Expose val fileName: String,
    var importedAt: Long,
    @Expose val type: PhotoType,
    @Expose var size: Long = 0L,
    @Expose val uuid: String = UUID.randomUUID().toString(),
    @PrimaryKey(autoGenerate = true) val id: Int? = null
) {

    val internalFileName: String
        get() = internalFileName(uuid)

    val internalThumbnailFileName: String
        get() = internalThumbnailFileName(uuid)

    val internalVideoPreviewFileName: String
        get() = internalVideoPreviewFileName(uuid)
}