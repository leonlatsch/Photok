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

import android.content.Context
import androidx.room.Room
import com.google.gson.GsonBuilder
import dev.leonlatsch.photok.backup.data.BackupMetaData
import dev.leonlatsch.photok.backup.data.ReadBackupMetadataUseCase
import dev.leonlatsch.photok.backup.data.toBackup
import dev.leonlatsch.photok.encryption.domain.crypto.CbcCryptoEngine
import dev.leonlatsch.photok.encryption.domain.crypto.KeyGen
import dev.leonlatsch.photok.encryption.domain.models.Algorithm
import dev.leonlatsch.photok.encryption.domain.models.Kdf
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionParams
import dev.leonlatsch.photok.encryption.domain.models.VaultSession
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.io.IO
import dev.leonlatsch.photok.io.VaultFileStorage
import dev.leonlatsch.photok.model.database.PhotokDatabase
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.model.database.entity.internalFileName
import dev.leonlatsch.photok.model.database.entity.internalVideoPreviewFileName
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Creates a backup, restores it, and checks the bytes survived the trip.
 *
 * Everything between the two ends is real: the zip, the CBC engine, the vault file storage and
 * the Room transaction the indexing phase runs in. Only [PhotoRepository] and [AlbumRepository]
 * are mocked, to see what the restore decided to index.
 */
@RunWith(RobolectricTestRunner::class)
class BackupRoundTripTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val keyGen = KeyGen()
    private val cryptoEngine = CbcCryptoEngine()
    private val io = IO(context)

    /** The vault the backup was written from, and the one it is restored into. */
    private val sourceSession = VaultSession(keyGen.generateVaultMasterKey())
    private val targetSession = VaultSession(keyGen.generateVaultMasterKey())

    private val photos = listOf(
        photo("holiday.jpg", PhotoType.JPEG),
        photo("clip.mp4", PhotoType.MP4),
    )

    private val video = photos[1]

    private val plaintext =
        photos.associate { it.uuid to "content of ${it.fileName}".toByteArray() }

    private lateinit var database: PhotokDatabase
    private lateinit var insertedPhotos: MutableList<Photo>
    private lateinit var backupStrategy: BackupStrategyImpl

    @Before
    fun setup() = runTest {
        insertedPhotos = mutableListOf()
        database = Room.inMemoryDatabaseBuilder(context, PhotokDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        // Fill the source vault: a main file per photo and a video preview for the video.
        val sourceStorage = vaultFileStorage(sourceSession)
        for (photo in photos) {
            sourceStorage.openEncryptedOutput(photo.internalFileName)!!
                .use { it.write(plaintext.getValue(photo.uuid)) }
        }
        sourceStorage.openEncryptedOutput(video.internalVideoPreviewFileName)!!
            .use { it.write("preview of ${video.fileName}".toByteArray()) }

        val dumpDatabase = mockk<DumpDatabaseUseCase>()
        coEvery { dumpDatabase(BackupMetaData.CURRENT_BACKUP_VERSION) } returns metaData()

        backupStrategy = BackupStrategyImpl(dumpDatabase, io, gson, context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `a backup restores the files it was created from`() = runTest {
        val archive = createBackup()

        val result = restoreBackup(archive, RestoreDuplicates.Skip(emptySet()))

        assertEquals(2, result.filesRestored)
        assertEquals(0, result.filesSkipped)
        assertEquals(emptyList<FailedFile>(), result.failedFiles)
        assertEquals(photos.map { it.uuid }.toSet(), insertedPhotos.map { it.uuid }.toSet())

        val targetStorage = vaultFileStorage(targetSession)
        for (photo in photos) {
            val bytes = targetStorage.openEncryptedInput(photo.internalFileName)!!
                .use { it.readBytes() }

            assertArrayEquals(
                "${photo.fileName} did not survive the round trip",
                plaintext.getValue(photo.uuid),
                bytes,
            )
        }
    }

    @Test
    fun `the restore keeps the original import dates`() = runTest {
        val archive = createBackup()

        restoreBackup(archive, RestoreDuplicates.Skip(emptySet()))

        assertEquals(
            photos.associate { it.uuid to it.importedAt },
            insertedPhotos.associate { it.uuid to it.importedAt },
        )
    }

    @Test
    fun `a skipped photo is neither written nor indexed`() = runTest {
        val archive = createBackup()
        val skipped = photos.first()

        val result = restoreBackup(archive, RestoreDuplicates.Skip(setOf(skipped.uuid)))

        assertEquals(1, result.filesRestored)
        assertEquals(1, result.filesSkipped)
        assertEquals(emptyList<FailedFile>(), result.failedFiles)
        assertEquals(listOf(photos[1].uuid), insertedPhotos.map { it.uuid })
    }

    @Test
    fun `a photo imported again gets its own uuid and files next to the original`() = runTest {
        val archive = createBackup()
        val duplicate = video
        val newUuid = "uuid-copy"

        val sourceStorage = vaultFileStorage(sourceSession)
        val originalBytes = sourceStorage.openEncryptedInput(duplicate.internalFileName)!!
            .use { it.readBytes() }

        val result = restoreBackup(
            archive,
            RestoreDuplicates.ImportAgain(mapOf(duplicate.uuid to newUuid)),
        )

        assertEquals(2, result.filesRestored)
        assertEquals(0, result.filesSkipped)
        assertEquals(emptyList<FailedFile>(), result.failedFiles)
        assertEquals(setOf(newUuid, photos[0].uuid), insertedPhotos.map { it.uuid }.toSet())

        val targetStorage = vaultFileStorage(targetSession)
        val copyBytes = targetStorage.openEncryptedInput(internalFileName(newUuid))!!
            .use { it.readBytes() }
        assertArrayEquals(plaintext.getValue(duplicate.uuid), copyBytes)

        val copyPreview = targetStorage.openEncryptedInput(internalVideoPreviewFileName(newUuid))!!
            .use { it.readBytes() }
        assertArrayEquals("preview of ${duplicate.fileName}".toByteArray(), copyPreview)

        val untouchedBytes = sourceStorage.openEncryptedInput(duplicate.internalFileName)!!
            .use { it.readBytes() }
        assertArrayEquals(originalBytes, untouchedBytes)
    }

    @Test
    fun `a photo missing from the archive is reported instead of dropped`() = runTest {
        val archive = createBackup().withoutEntry(photos.first().internalFileName)

        val result = restoreBackup(archive, RestoreDuplicates.Skip(emptySet()))

        assertEquals(1, result.filesRestored)
        assertEquals(listOf(photos.first().fileName), result.failedFiles.map { it.fileName })
        assertEquals(listOf(photos[1].uuid), insertedPhotos.map { it.uuid })
    }

    /** A fresh runner, so abort can only work off the metadata and not off state from the run. */
    @Test
    fun `abort deletes the files of a restore that was not indexed`() = runTest {
        val archive = createBackup()
        val newUuid = "uuid-copy"
        val duplicates = RestoreDuplicates.ImportAgain(mapOf(video.uuid to newUuid))

        restoreBackup(archive, duplicates)
        insertedPhotos.clear()

        runner().abort(metaData(), duplicates)

        val targetStorage = vaultFileStorage(targetSession)
        for (uuid in listOf(photos[0].uuid, newUuid)) {
            assertFalse(targetStorage.encryptedFileExists(internalFileName(uuid)))
        }
        assertFalse(targetStorage.encryptedFileExists(internalVideoPreviewFileName(newUuid)))
    }

    @Test
    fun `abort keeps the files of indexed and skipped photos`() = runTest {
        val archive = createBackup()
        val duplicates = RestoreDuplicates.Skip(setOf(photos[0].uuid))

        restoreBackup(archive, duplicates)

        runner().abort(metaData(), duplicates)

        val targetStorage = vaultFileStorage(targetSession)
        for (photo in photos) {
            assertTrue(targetStorage.encryptedFileExists(photo.internalFileName))
        }
        assertTrue(targetStorage.encryptedFileExists(video.internalVideoPreviewFileName))
    }

    @Test
    fun `only the files of the photo are written, no stray vault files`() = runTest {
        context.openFileOutput("leftover.photok", Context.MODE_PRIVATE)
            .use { it.write("not part of a V5 backup".toByteArray()) }

        val entries = createBackup().entryNames()

        assertEquals(
            listOf(
                BackupMetaData.FILE_NAME,
                photos[0].internalFileName,
                photos[1].internalFileName,
                photos[1].internalVideoPreviewFileName,
            ),
            entries,
        )
    }

    @Test
    fun `thumbnails of old backups are skipped instead of written into the vault`() = runTest {
        val thumbnails = photos.map { it.internalThumbnailFileName }
        val archive = createBackup().withEncryptedEntries(thumbnails)

        val result = restoreBackup(archive, RestoreDuplicates.Skip(emptySet()))

        assertEquals(2, result.filesRestored)
        assertEquals(emptyList<FailedFile>(), result.failedFiles)
        for (thumbnail in thumbnails) {
            assertFalse(context.getFileStreamPath(thumbnail).exists())
        }
    }

    private suspend fun createBackup(): ByteArray {
        val out = ByteArrayOutputStream()

        ZipOutputStream(out).use { zip ->
            backupStrategy.createMetaFileInBackup(zip).getOrThrow()
            for (photo in photos) {
                backupStrategy.writePhotoToBackup(photo, zip).getOrThrow()
            }
        }

        return out.toByteArray()
    }

    private suspend fun restoreBackup(
        archive: ByteArray,
        duplicates: RestoreDuplicates,
    ): RestoreResult {
        val metaData = ZipInputStream(ByteArrayInputStream(archive)).use { zip ->
            zip.nextEntry
            ReadBackupMetadataUseCase(gson)(zip)
        } as BackupMetaData.V5

        val progress = ZipInputStream(ByteArrayInputStream(archive)).use { zip ->
            runner().run(RestoreBackupV5(cryptoEngine), metaData, zip, sourceSession, duplicates)
                .toList()
        }

        return (progress.last() as RestoreProgress.Finished).result
    }

    private fun runner() = RestoreBackupRunner(
        io = io,
        vaultFileStorage = vaultFileStorage(targetSession),
        photoRepository = photoRepository(),
        albumRepository = mockk<AlbumRepository>(relaxed = true),
        database = database,
    )

    private fun vaultFileStorage(session: VaultSession) = VaultFileStorage(
        sessionRepository = mockk { every { get() } returns session },
        cryptoEngine = cryptoEngine,
        app = RuntimeEnvironment.getApplication(),
    )

    private fun photoRepository() = mockk<PhotoRepository> {
        coEvery { insert(any()) } answers {
            insertedPhotos += firstArg<Photo>()
            1L
        }
        coEvery { insertAll(any()) } answers {
            insertedPhotos += firstArg<List<Photo>>()
        }
        coEvery { getAllUuids() } answers { insertedPhotos.map { it.uuid } }
    }

    private fun photo(fileName: String, type: PhotoType) = Photo(
        fileName = fileName,
        importedAt = 1_700_000_000_000L + fileName.length,
        type = type,
        size = "content of $fileName".toByteArray().size.toLong(),
        lastModified = null,
        uuid = "uuid-${fileName.substringBefore('.')}",
    )

    private fun metaData() = BackupMetaData.V5(
        photos = photos.map { it.toBackup() },
        albums = emptyList(),
        albumPhotoRefs = emptyList(),
        createdAt = 1_700_000_000_000L,
        backupVersion = BackupMetaData.CURRENT_BACKUP_VERSION,
        wrappedVMK = "",
        params = VaultProtectionParams(
            salt = "salt",
            iv = "iv",
            kdf = Kdf.PBKDF2WithHmacSHA256,
            kdfIterations = 100_000,
            algorithm = Algorithm.AesCbcPkcs7Padding,
            keySize = 256,
        ),
    )

    private fun ByteArray.entryNames(): List<String> {
        val names = mutableListOf<String>()

        ZipInputStream(ByteArrayInputStream(this)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                names += entry.name
                entry = zip.nextEntry
            }
        }

        return names
    }

    /** Adds entries encrypted with the source vault, the way backups of older versions had them. */
    private fun ByteArray.withEncryptedEntries(names: List<String>): ByteArray {
        val sourceStorage = vaultFileStorage(sourceSession)
        val encryptedEntries = names.associateWith { name ->
            sourceStorage.openEncryptedOutput(name)!!.use { it.write("content of $name".toByteArray()) }
            val encrypted = context.openFileInput(name).use { it.readBytes() }
            context.deleteFile(name)
            encrypted
        }

        val out = ByteArrayOutputStream()

        ZipOutputStream(out).use { zipOut ->
            ZipInputStream(ByteArrayInputStream(this)).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    zipOut.putNextEntry(ZipEntry(entry.name))
                    zipIn.copyTo(zipOut)
                    zipOut.closeEntry()
                    entry = zipIn.nextEntry
                }
            }

            for ((name, encrypted) in encryptedEntries) {
                zipOut.putNextEntry(ZipEntry(name))
                zipOut.write(encrypted)
                zipOut.closeEntry()
            }
        }

        return out.toByteArray()
    }

    /** Drops one entry, the way a damaged or hand edited archive would. */
    private fun ByteArray.withoutEntry(name: String): ByteArray {
        val out = ByteArrayOutputStream()

        ZipOutputStream(out).use { zipOut ->
            ZipInputStream(ByteArrayInputStream(this)).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    if (entry.name != name) {
                        zipOut.putNextEntry(ZipEntry(entry.name))
                        zipIn.copyTo(zipOut)
                        zipOut.closeEntry()
                    }
                    entry = zipIn.nextEntry
                }
            }
        }

        return out.toByteArray()
    }
}
