


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

    suspend fun preBackup() {}
    suspend fun postBackup() {}

    suspend fun writePhotoToBackup(
        photo: Photo,
        zipOutputStream: ZipOutputStream,
    ): Result<Unit>

    suspend fun createMetaFileInBackup(
        zipOutputStream: ZipOutputStream
    ): Result<Unit>
}

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

    suspend fun preBackup() {}
    suspend fun postBackup() {}

    suspend fun writePhotoToBackup(
        photo: Photo,
        zipOutputStream: ZipOutputStream,
    ): Result<Unit>

    suspend fun createMetaFileInBackup(
        zipOutputStream: ZipOutputStream
    ): Result<Unit>
}