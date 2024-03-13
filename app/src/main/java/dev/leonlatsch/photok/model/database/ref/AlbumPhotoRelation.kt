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

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.Relation
import dev.leonlatsch.photok.model.database.entity.Album
import dev.leonlatsch.photok.model.database.entity.Photo

private const val ALBUM_ID = "albumId"
private const val PHOTO_ID = "albumId"

@Entity(primaryKeys = [ALBUM_ID, PHOTO_ID])
data class AlbumPhotosCrossRef(
    val albumId: Int,
    val photoId: Int
)

data class AlbumWithPhotos(
    @Embedded val playlist: Album,
    @Relation(
        parentColumn = ALBUM_ID,
        entityColumn = PHOTO_ID,
        associateBy = Junction(AlbumPhotosCrossRef::class)
    )
    val photos: List<Photo>
)