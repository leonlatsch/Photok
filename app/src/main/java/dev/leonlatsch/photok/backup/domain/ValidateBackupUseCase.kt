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
import javax.inject.Inject

class ValidateBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository,
) {

    suspend operator fun invoke(uri: Uri): Result<BackupValidation> {
        val zipInputStream = backupRepository.openBackupInput(uri)

        var files = 0
        var metaData: BackupMetaData? = null
        var backupVersion: Int? = null

        var ze = zipInputStream.nextEntry
        while (ze != null) {
            if (ze.name == BackupMetaData.FILE_NAME) {
                metaData = backupRepository.readBackupMetadata(zipInputStream)
                backupVersion = metaData.getBackupVersion()
            } else if (ze.name.endsWith(".photok")) {
                files++
            }

            ze = zipInputStream.nextEntry
        }

        return if (metaData != null && backupVersion != null && files > 0) {
            val backupFileDetails = backupRepository.getBackupFileDetails(uri)
            val backupValidation = BackupValidation(
                metaData = metaData,
                backupFileDetails = backupFileDetails,
            )
            Result.success(backupValidation)
        } else {
            Result.failure(IllegalStateException(""))
        }
    }
}

private fun BackupMetaData?.getBackupVersion(): Int {
    this?.let {
        return if (it.backupVersion == 0) { // Treat legacy version 0 as 1
            1
        } else {
            it.backupVersion
        }
    }

    return -1
}

data class BackupValidation(
    val metaData: BackupMetaData,
    val backupFileDetails: BackupFileDetails,
)