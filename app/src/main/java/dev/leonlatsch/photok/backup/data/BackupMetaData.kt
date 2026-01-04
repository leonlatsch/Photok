


package dev.leonlatsch.photok.backup.data

import com.google.gson.annotations.Expose
import dev.leonlatsch.photok.model.database.entity.PhotoType

/**
 * Model for meta.json in backups.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
data class BackupMetaData(
    @Expose val password: String,
    @Expose val photos: List<PhotoBackup>,
    @Expose val albums: List<AlbumBackup>,
    @Expose val albumPhotoRefs: List<AlbumPhotoRefBackup>,
    @Expose val createdAt: Long = System.currentTimeMillis(),
    @Expose val backupVersion: Int,
) {
    companion object {
        const val FILE_NAME = "meta.json"

        /**
         * Backup version used before switching the encryption. Used for creating a backup before migrating.
         */
        const val LEGACY_BACKUP_VERSION = 3
        const val CURRENT_BACKUP_VERSION = 4

        val VALID_BACKUP_VERSIONS = arrayOf(1, 2, 3, 4)
    }
}

fun BackupMetaData.getPhotosInOriginalOrder(): List<PhotoBackup> {
    return photos.sortedBy {
        it.importedAt
    } // ASC to keep original order. Dump is created with DESC
}

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

package dev.leonlatsch.photok.backup.data

import com.google.gson.annotations.Expose
import dev.leonlatsch.photok.model.database.entity.PhotoType

/**
 * Model for meta.json in backups.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
data class BackupMetaData(
    @Expose val password: String,
    @Expose val photos: List<PhotoBackup>,
    @Expose val albums: List<AlbumBackup>,
    @Expose val albumPhotoRefs: List<AlbumPhotoRefBackup>,
    @Expose val createdAt: Long = System.currentTimeMillis(),
    @Expose val backupVersion: Int,
) {
    companion object {
        const val FILE_NAME = "meta.json"

        /**
         * Backup version used before switching the encryption. Used for creating a backup before migrating.
         */
        const val LEGACY_BACKUP_VERSION = 3
        const val CURRENT_BACKUP_VERSION = 4

        val VALID_BACKUP_VERSIONS = arrayOf(1, 2, 3, 4)
    }
}

fun BackupMetaData.getPhotosInOriginalOrder(): List<PhotoBackup> {
    return photos.sortedBy {
        it.importedAt
    } // ASC to keep original order. Dump is created with DESC
}

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