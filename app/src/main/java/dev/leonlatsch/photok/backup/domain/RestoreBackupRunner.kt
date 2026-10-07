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

import androidx.room.withTransaction
import dev.leonlatsch.photok.backup.data.BackupMetaData
import dev.leonlatsch.photok.backup.data.PhotoBackup
import dev.leonlatsch.photok.backup.data.getPhotosInOriginalOrder
import dev.leonlatsch.photok.backup.data.toDomain
import dev.leonlatsch.photok.encryption.domain.models.Session
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.io.IO
import dev.leonlatsch.photok.io.VaultFileStorage
import dev.leonlatsch.photok.model.database.PhotokDatabase
import dev.leonlatsch.photok.model.database.entity.THUMBNAIL_SUFFIX
import dev.leonlatsch.photok.model.database.entity.internalFileName
import dev.leonlatsch.photok.model.database.entity.internalVideoPreviewFileName
import dev.leonlatsch.photok.model.database.entity.isMainFileName
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.zip.ZipInputStream
import javax.inject.Inject

/** A photo that meta.json lists but that has no entry in the archive. */
class MissingFromArchive(fileName: String) :
    Exception("$fileName is listed in the backup but missing from the archive")

/**
 * Runs one restore.
 *
 * Every format from V2 on is restored the same way — walk the archive, decrypt each entry with
 * the backup's key, write it back out with the vault's key, then index what landed. The only
 * things that differ are which cipher opens an entry and what the file is called inside the
 * vault, and those come from the [RestoreBackupStrategy].
 *
 * Thumbnails in the archive are skipped. They are created in the cache dir when needed.
 *
 * V1 is not restored through here. It has no thumbnails in the archive and has to regenerate
 * them from the decoded image, which is a different shape of work.
 */
class RestoreBackupRunner @Inject constructor(
    private val io: IO,
    private val vaultFileStorage: VaultFileStorage,
    private val photoRepository: PhotoRepository,
    private val albumRepository: AlbumRepository,
    private val database: PhotokDatabase,
) {

    fun <T : BackupMetaData> run(
        strategy: RestoreBackupStrategy<T>,
        metaData: T,
        stream: ZipInputStream,
        session: Session,
        duplicates: RestoreDuplicates,
    ): Flow<RestoreProgress> = channelFlow {
        val start = System.currentTimeMillis()

        val photosToRestore = metaData.photos.filterNot { duplicates.isSkipped(it.uuid) }

        val failedFiles = mutableListOf<FailedFile>()
        val restoredUuids = mutableSetOf<String>()
        val seenUuids = mutableSetOf<String>()

        val tracker = RestoreProgressTracker(metaData.photos)
        send(tracker.snapshot())

        fun recordFailure(photo: PhotoBackup, cause: Throwable?) {
            if (failedFiles.none { it.fileName == photo.fileName }) {
                failedFiles += FailedFile(photo.fileName, cause)
            }
        }

        var ze = stream.nextEntry

        while (ze != null) {
            val entryName = ze.name

            if (entryName == BackupMetaData.FILE_NAME) {
                ze = stream.nextEntry
                continue
            }

            // Files that are not mentioned in the metadata might be dead files from old
            // versions of photok.
            val photoBackup = metaData.photos.find { entryName.contains(it.uuid) }
            if (photoBackup == null) {
                Timber.i("Skipping dead file in backup: $entryName")
                ze = stream.nextEntry
                continue
            }

            // Thumbnails are skipped. They are generated on demand.
            if (entryName.endsWith(THUMBNAIL_SUFFIX)) {
                ze = stream.nextEntry
                continue
            }

            if (duplicates.isSkipped(photoBackup.uuid)) {
                ze = stream.nextEntry

                if (isMainFileName(entryName)) {
                    // Track skip AFTER actually skipped
                    tracker.skipFile(photoBackup)?.let { trySend(it) }
                }

                continue
            }

            val isMainFile = isMainFileName(entryName)
            if (isMainFile) {
                seenUuids += photoBackup.uuid
                tracker.startFile(photoBackup)
            }

            val internalFileName = strategy.internalFileName(entryName)
                .replace(photoBackup.uuid, duplicates.vaultUuid(photoBackup.uuid))

            val decryptedInput = strategy.decrypt(stream, session)
            if (decryptedInput == null) {
                Timber.e("Could not open the decrypt stream for zip entry: $entryName")

                if (isMainFile) {
                    recordFailure(photoBackup, null)
                    send(tracker.finishFile())
                }

                ze = stream.nextEntry
                continue
            }

            val internalOutput = vaultFileStorage.openEncryptedOutput(internalFileName)
            if (internalOutput == null) {
                Timber.e("Could not open the vault output for zip entry: $entryName")

                if (isMainFile) {
                    recordFailure(photoBackup, null)
                    send(tracker.finishFile())
                }

                ze = stream.nextEntry
                continue
            }

            io.copy(decryptedInput, internalOutput) { chunk ->
                if (isMainFile) {
                    tracker.advance(chunk)?.let { trySend(it) }
                } else {
                    tracker.advanceSidecar(chunk)
                }
            }
                .onSuccess {
                    // A photo counts as restored once its own bytes are on disk. A broken
                    // thumbnail is still reported, but does not cost the photo its database row.
                    if (isMainFile) restoredUuids += photoBackup.uuid
                }
                .onFailure {
                    Timber.e(it, "Error restoring zip entry: $entryName")
                    recordFailure(photoBackup, it)
                }

            if (isMainFile) send(tracker.finishFile())

            ze = stream.nextEntry
        }

        photosToRestore
            .filter { it.uuid !in seenUuids }
            .forEach {
                Timber.e("Photo listed in meta.json but missing from the archive: ${it.fileName}")
                recordFailure(it, MissingFromArchive(it.fileName))
            }

        send(tracker.snapshot())

        send(RestoreProgress.Indexing)

        // One transaction, so canceling in the middle of indexing does not leave half the rows
        // behind. The files are cleaned up by abort().
        database.withTransaction {
            metaData.getPhotosInOriginalOrder()
                .filter { it.uuid in restoredUuids }
                .map { it.toDomain().copy(uuid = duplicates.vaultUuid(it.uuid)) }
                .let { photoRepository.insertAll(it) }

            metaData.albums.forEach { albumRepository.createAlbum(it.toDomain()) }

            metaData.albumPhotoRefs
                .filter { it.photoUUID in restoredUuids }
                .map { it.toDomain().copy(photoUUID = duplicates.vaultUuid(it.photoUUID)) }
                .forEach { albumRepository.link(it) }
        }

        Timber.d("PERFORMANCE: Restore backup took ${System.currentTimeMillis() - start}ms")

        send(
            RestoreProgress.Finished(
                RestoreResult(
                    filesRestored = restoredUuids.size,
                    filesTotal = metaData.photos.size,
                    filesSkipped = metaData.photos.count { duplicates.isSkipped(it.uuid) },
                    albumsRestored = metaData.albums.size,
                    durationMillis = System.currentTimeMillis() - start,
                    failedFiles = failedFiles,
                )
            )
        )
    }.flowOn(Dispatchers.IO)

    /**
     * Deletes the files a canceled [run] of the same [metaData] and [duplicates] left behind.
     *
     * Derived from the metadata instead of tracked during [run], so it also works when the run
     * was never observed to the end. Photos that already have a database row are left alone,
     * the indexing transaction committed for them.
     */
    suspend fun abort(
        metaData: BackupMetaData,
        duplicates: RestoreDuplicates,
    ) = withContext(Dispatchers.IO) {
        val indexedUuids = photoRepository.getAllUuids().toSet()

        metaData.photos
            .asSequence()
            .filterNot { duplicates.isSkipped(it.uuid) }
            .map { duplicates.vaultUuid(it.uuid) }
            .filterNot { it in indexedUuids }
            .flatMap {
                listOf(
                    internalFileName(it),
                    internalVideoPreviewFileName(it),
                )
            }
            .filter { vaultFileStorage.encryptedFileExists(it) }
            .toList()
            .forEach { vaultFileStorage.deleteEncryptedFile(it) }
    }
}
