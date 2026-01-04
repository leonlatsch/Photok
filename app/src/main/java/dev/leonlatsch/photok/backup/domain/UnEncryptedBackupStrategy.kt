


package dev.leonlatsch.photok.backup.domain

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.model.database.entity.LEGACY_PHOTOK_FILE_EXTENSION
import dev.leonlatsch.photok.model.database.entity.PHOTOK_FILE_EXTENSION
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.io.IO
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.security.migration.LegacyEncryptionManager
import java.io.InputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnEncryptedBackupStrategy @Inject constructor(
    @ApplicationContext private val context: Context,
    private val io: IO,
    private val encryptionManager: EncryptionManager,
    @LegacyEncryptionManager private val legacyEncryptionManager: EncryptionManager,
) : BackupStrategy {

    private val usedFilenames = mutableListOf<String>()

    override suspend fun writePhotoToBackup(
        photo: Photo,
        zipOutputStream: ZipOutputStream
    ): Result<Unit> {
        val filename = if (usedFilenames.toList().contains(photo.fileName)) {
            photo.fileName + "-copy"
        } else {
            photo.fileName
        }

        val input = getInputStreamForPhoto(photo)

        input ?: return Result.failure(IllegalStateException("Input stream missing for photo"))

        return io.zip.writeZipEntry(filename, input, zipOutputStream).also {
            usedFilenames.add(filename)
        }
    }

    override suspend fun createMetaFileInBackup(zipOutputStream: ZipOutputStream): Result<Unit> {
        return Result.success(Unit)
    }

    private fun getInputStreamForPhoto(photo: Photo): InputStream? {
        val allFiles = context.fileList()

        for (file in allFiles) {
            if (!file.contains(photo.uuid)) {
                continue
            }

            if (file.endsWith(LEGACY_PHOTOK_FILE_EXTENSION)) {
                val encryptedInput =  context.openFileInput(file)
                return legacyEncryptionManager.createCipherInputStream(encryptedInput)
            }

            if (file.endsWith(PHOTOK_FILE_EXTENSION)) {
                val encryptedInput =  context.openFileInput(file)
                return encryptionManager.createCipherInputStream(encryptedInput)
            }

        }

        return null
    }

    override suspend fun postBackup() {
        super.postBackup()

        usedFilenames.clear()
    }
}

package dev.leonlatsch.photok.backup.domain

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.model.database.entity.LEGACY_PHOTOK_FILE_EXTENSION
import dev.leonlatsch.photok.model.database.entity.PHOTOK_FILE_EXTENSION
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.io.IO
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.security.migration.LegacyEncryptionManager
import java.io.InputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnEncryptedBackupStrategy @Inject constructor(
    @ApplicationContext private val context: Context,
    private val io: IO,
    private val encryptionManager: EncryptionManager,
    @LegacyEncryptionManager private val legacyEncryptionManager: EncryptionManager,
) : BackupStrategy {

    private val usedFilenames = mutableListOf<String>()

    override suspend fun writePhotoToBackup(
        photo: Photo,
        zipOutputStream: ZipOutputStream
    ): Result<Unit> {
        val filename = if (usedFilenames.toList().contains(photo.fileName)) {
            photo.fileName + "-copy"
        } else {
            photo.fileName
        }

        val input = getInputStreamForPhoto(photo)

        input ?: return Result.failure(IllegalStateException("Input stream missing for photo"))

        return io.zip.writeZipEntry(filename, input, zipOutputStream).also {
            usedFilenames.add(filename)
        }
    }

    override suspend fun createMetaFileInBackup(zipOutputStream: ZipOutputStream): Result<Unit> {
        return Result.success(Unit)
    }

    private fun getInputStreamForPhoto(photo: Photo): InputStream? {
        val allFiles = context.fileList()

        for (file in allFiles) {
            if (!file.contains(photo.uuid)) {
                continue
            }

            if (file.endsWith(LEGACY_PHOTOK_FILE_EXTENSION)) {
                val encryptedInput =  context.openFileInput(file)
                return legacyEncryptionManager.createCipherInputStream(encryptedInput)
            }

            if (file.endsWith(PHOTOK_FILE_EXTENSION)) {
                val encryptedInput =  context.openFileInput(file)
                return encryptionManager.createCipherInputStream(encryptedInput)
            }

        }

        return null
    }

    override suspend fun postBackup() {
        super.postBackup()

        usedFilenames.clear()
    }
}