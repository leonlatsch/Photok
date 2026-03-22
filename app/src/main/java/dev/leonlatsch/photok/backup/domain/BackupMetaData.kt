/*
 *   Copyright 2020-2026 Leon Latsch
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

import com.google.gson.annotations.Expose

const val LEGACY_BACKUP_VERSION = 3
const val CURRENT_BACKUP_VERSION = 5
const val META_JSON_FILENAME = "meta.json"

sealed interface BackupMetaData {
    val photos: List<PhotoBackup>
    val createdAt: Long
    val backupVersion: Int

    data class V1(
        @Expose val password: String,
        @Expose override val photos: List<PhotoBackup>,
        @Expose override val createdAt: Long = System.currentTimeMillis(),
        @Expose override val backupVersion: Int,
    ) : BackupMetaData

    data class V2(
        @Expose val password: String,
        @Expose override val photos: List<PhotoBackup>,
        @Expose override val createdAt: Long = System.currentTimeMillis(),
        @Expose override val backupVersion: Int,
    ) : BackupMetaData

    data class V3(
        @Expose val password: String,
        @Expose override val photos: List<PhotoBackup>,
        @Expose val albums: List<AlbumBackup>,
        @Expose val albumPhotoRefs: List<AlbumPhotoRefBackup>,
        @Expose override val createdAt: Long = System.currentTimeMillis(),
        @Expose override val backupVersion: Int,
    ) : BackupMetaData

    data class V4(
        @Expose val password: String,
        @Expose override val photos: List<PhotoBackup>,
        @Expose val albums: List<AlbumBackup>,
        @Expose val albumPhotoRefs: List<AlbumPhotoRefBackup>,
        @Expose override val createdAt: Long = System.currentTimeMillis(),
        @Expose override val backupVersion: Int,
    ) : BackupMetaData

    data class V5(
        @Expose val vault: VaultBackup,
        @Expose override val photos: List<PhotoBackup>,
        @Expose val albums: List<AlbumBackup>,
        @Expose val albumPhotoRefs: List<AlbumPhotoRefBackup>,
        @Expose override val createdAt: Long = System.currentTimeMillis(),
        @Expose override val backupVersion: Int,
    ) : BackupMetaData
}

fun BackupMetaData.getPhotosInOriginalOrder(): List<PhotoBackup> {
    return photos.sortedBy {
        it.importedAt
    } // ASC to keep original order. Dump is created with DESC
}
