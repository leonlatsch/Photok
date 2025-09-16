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

package dev.leonlatsch.photok.backup.domain

import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.model.database.entity.Photo
import java.util.zip.ZipOutputStream

interface BackupStrategy {

    enum class Name(val title: Int) {
        Default(R.string.backup_processing_title),
        Legacy(R.string.backup_processing_title),
        UnEncrypted(R.string.migration_error_extracting_title);
    }

    suspend fun writePhotoToBackup(
        photo: Photo,
        zipOutputStream: ZipOutputStream,
    ): Result<Unit>

    suspend fun createMetaFileInBackup(
        zipOutputStream: ZipOutputStream
    ): Result<Unit>
}