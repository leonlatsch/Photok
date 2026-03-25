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

package dev.leonlatsch.photok.backup.domain

import dev.leonlatsch.photok.model.database.entity.PhotoType


data class VaultBackup(
    val uuid: String,
    val name: String,
    val userSalt: String,
    val verifier: String,
    val iv: String,
)

data class PhotoBackup(
    val fileName: String,
    val importedAt: Long,
    val lastModified: Long?,
    val type: PhotoType,
    val size: Long,
    val uuid: String,
)

data class AlbumBackup(
    val uuid: String,
    val name: String,
    val modifiedAt: Long?,
)

data class AlbumPhotoRefBackup(
    val albumUUID: String,
    val photoUUID: String,
    val linkedAt: Long,
)