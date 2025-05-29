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

import android.net.Uri
import dev.leonlatsch.photok.backup.data.BackupMetaData
import dev.leonlatsch.photok.backup.domain.model.BackupFileDetails
import dev.leonlatsch.photok.model.database.entity.Photo
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

interface BackupRepository {
    suspend fun openBackupInput(uri: Uri): ZipInputStream
    suspend fun openBackupOutput(uri: Uri): ZipOutputStream
    suspend fun writePhoto(photo: Photo, zipOutputStream: ZipOutputStream): Result<Unit>
    suspend fun writeBackupMetadata(
        backupMetaData: BackupMetaData,
        zipOutputStream: ZipOutputStream,
    ): Result<Unit>

    suspend fun readBackupMetadata(zipInputStream: ZipInputStream, ): BackupMetaData
    suspend fun getBackupFileDetails(uri: Uri): BackupFileDetails
}