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

package dev.leonlatsch.photok.model.database.ref

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.Relation
import dev.leonlatsch.photok.model.database.entity.AlbumTable
import dev.leonlatsch.photok.model.database.entity.Photo

private const val ALBUM_UUID = "album_uuid"
private const val PHOTO_UUID = "photo_uuid"

@Entity(
    primaryKeys = [ALBUM_UUID, PHOTO_UUID],
    tableName = "album_photos_cross_ref",
)
data class AlbumPhotoCroffRefTable(
    @ColumnInfo(name = ALBUM_UUID) val albumUUID: String,
    @ColumnInfo(name = PHOTO_UUID, index = true) val photoUUID: String,

    @ColumnInfo(name = "linked_at")
    val linkedAt: Long
)

data class AlbumWithPhotos(
    @Embedded val album: AlbumTable,
    @Relation(
        parentColumn = ALBUM_UUID,
        entityColumn = PHOTO_UUID,
        associateBy = Junction(AlbumPhotoCroffRefTable::class)
    )
    val photos: List<Photo>
)