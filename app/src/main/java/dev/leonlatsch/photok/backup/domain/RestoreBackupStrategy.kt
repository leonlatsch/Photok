


package dev.leonlatsch.photok.backup.domain

import dev.leonlatsch.photok.backup.data.BackupMetaData
import java.util.zip.ZipInputStream

interface RestoreBackupStrategy {
    suspend fun restore(
        metaData: BackupMetaData,
        stream: ZipInputStream,
        originalPassword: String,
    ): RestoreResult
}


package dev.leonlatsch.photok.backup.domain

import dev.leonlatsch.photok.backup.data.BackupMetaData
import java.util.zip.ZipInputStream

interface RestoreBackupStrategy {
    suspend fun restore(
        metaData: BackupMetaData,
        stream: ZipInputStream,
        originalPassword: String,
    ): RestoreResult
}
