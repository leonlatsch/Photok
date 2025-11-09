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

package dev.leonlatsch.photok.backup.data

import dev.leonlatsch.photok.gallery.albums.domain.model.Album
import dev.leonlatsch.photok.gallery.albums.domain.model.AlbumPhotoRef
import dev.leonlatsch.photok.model.database.entity.Photo

fun Photo.toBackup(): PhotoBackup =
    PhotoBackup(
        fileName = fileName,
        importedAt = importedAt,
        type = type,
        size = size,
        uuid = uuid,
    )

fun PhotoBackup.toDomain(): Photo =
    Photo(
        fileName = fileName,
        importedAt = importedAt,
        type = type,
        size = size,
        uuid = uuid,
    )

fun Album.toBackup(): AlbumBackup =
    AlbumBackup(
        uuid = uuid,
        modifiedAt = modifiedAt,
        name = name,
    )

fun AlbumBackup.toDomain(): Album =
    Album(
        uuid = uuid,
        name = name,
        modifiedAt = modifiedAt ?: System.currentTimeMillis(),
        files = emptyList(),
    )

fun AlbumPhotoRef.toBackup(): AlbumPhotoRefBackup =
    AlbumPhotoRefBackup(
        albumUUID = albumUUID,
        photoUUID = photoUUID,
        linkedAt = linkedAt,
    )

fun AlbumPhotoRefBackup.toDomain(): AlbumPhotoRef =
    AlbumPhotoRef(
        albumUUID = albumUUID,
        photoUUID = photoUUID,
        linkedAt = linkedAt,
    )