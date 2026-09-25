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

package dev.leonlatsch.photok.backup.data

import com.google.gson.Gson
import dev.leonlatsch.photok.backup.domain.BackupValidationError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.util.zip.ZipInputStream
import javax.inject.Inject

data class BackupHeader(
    val backupVersion: Int
)

class ReadBackupMetadataUseCase @Inject constructor(
    private val gson: Gson
) {
    suspend operator fun invoke(zipInputStream: ZipInputStream): BackupMetaData =
        withContext(Dispatchers.IO) {
            val json = InputStreamReader(zipInputStream).readText()

            val header = gson.fromJson(json, BackupHeader::class.java)
                ?: error("Empty meta json")

            val metaData = when (header.backupVersion) {
                0, 1 -> gson.fromJson(json, BackupMetaData.V1::class.java)
                    ?.withEmptyAlbums()
                    ?.copy(backupVersion = 1)
                2 -> gson.fromJson(json, BackupMetaData.V2::class.java)?.withEmptyAlbums()
                3 -> gson.fromJson(json, BackupMetaData.V3::class.java)
                4 -> gson.fromJson(json, BackupMetaData.V4::class.java)
                5 -> gson.fromJson(json, BackupMetaData.V5::class.java)

                else -> throw BackupValidationError.UnsupportedVersion(header.backupVersion)
            }

            metaData ?: error("Error reading meta json from $zipInputStream")
        }
}

// Albums added in V3. Fake the missing field as empty

private fun BackupMetaData.V1.withEmptyAlbums(): BackupMetaData.V1 {
    val albums: List<AlbumBackup>? = albums
    val albumPhotoRefs: List<AlbumPhotoRefBackup>? = albumPhotoRefs

    return copy(
        albums = albums.orEmpty(),
        albumPhotoRefs = albumPhotoRefs.orEmpty(),
    )
}

private fun BackupMetaData.V2.withEmptyAlbums(): BackupMetaData.V2 {
    val albums: List<AlbumBackup>? = albums
    val albumPhotoRefs: List<AlbumPhotoRefBackup>? = albumPhotoRefs

    return copy(
        albums = albums.orEmpty(),
        albumPhotoRefs = albumPhotoRefs.orEmpty(),
    )
}
