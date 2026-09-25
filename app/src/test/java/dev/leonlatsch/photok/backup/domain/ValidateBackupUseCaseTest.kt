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

import android.net.Uri
import com.google.gson.GsonBuilder
import dev.leonlatsch.photok.backup.data.BackupMetaData
import dev.leonlatsch.photok.backup.data.ReadBackupMetadataUseCase
import dev.leonlatsch.photok.io.IO
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Validation runs before anything is shown about a backup, and on a multi gigabyte archive it
 * must not read the whole file to do it. These pin down what it accepts, what it rejects, and
 * that the common case only touches the first entry.
 */
@RunWith(RobolectricTestRunner::class)
class ValidateBackupUseCaseTest {

    private val uri = Uri.parse("content://test/backup.zip")

    private val gson = GsonBuilder().create()
    private val io = mockk<IO>()

    private val validateBackup = ValidateBackupUseCase(ReadBackupMetadataUseCase(gson), io)

    @Test
    fun `accepts a backup and reports its size against the free space`() = runTest {
        val archive = archive(metaFirst = true, photoCount = 2)
        stubIO(archive, usableBytes = 10_000L)

        val validation = validateBackup(uri).getOrThrow()

        assertEquals(5, validation.metaData.backupVersion)
        assertEquals(2, validation.metaData.photos.size)
        assertEquals(2_000L, validation.requiredBytes)
        assertEquals(false, validation.notEnoughSpace)
    }

    @Test
    fun `flags a backup that does not fit into the remaining space`() = runTest {
        val archive = archive(metaFirst = true, photoCount = 2)
        stubIO(archive, usableBytes = 500L)

        val validation = validateBackup(uri).getOrThrow()

        assertEquals(true, validation.notEnoughSpace)
    }

    @Test
    fun `reads only the first entry when the meta file comes first`() = runTest {
        val archive = archive(metaFirst = true, photoCount = 2)
        val opened = stubIO(archive, usableBytes = 10_000L)

        validateBackup(uri).getOrThrow()

        assertEquals("Only one pass over the archive is needed", 1, opened.size)
        assertTrue("The zip stream must be closed", opened.all { it.closed })
    }

    @Test
    fun `still finds the meta file in an old backup that wrote it last`() = runTest {
        val archive = archive(metaFirst = false, photoCount = 2)
        stubIO(archive, usableBytes = 10_000L)

        val validation = validateBackup(uri).getOrThrow()

        assertEquals(2, validation.metaData.photos.size)
    }

    @Test
    fun `rejects a truncated archive`() = runTest {
        val archive = archive(metaFirst = true, photoCount = 2)
        stubIO(archive, usableBytes = 10_000L, hasDirectory = false)

        val error = validateBackup(uri).exceptionOrNull()

        assertTrue("Expected IncompleteFile, got $error", error is BackupValidationError.IncompleteFile)
    }

    @Test
    fun `rejects an archive without a meta file`() = runTest {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            zip.putNextEntry(ZipEntry("uuid-0.crypt"))
            zip.write(ByteArray(10))
            zip.closeEntry()
        }
        stubIO(out.toByteArray(), usableBytes = 10_000L)

        val error = validateBackup(uri).exceptionOrNull()

        assertTrue("Expected NoMetaData, got $error", error is BackupValidationError.NoMetaData)
    }

    @Test
    fun `rejects an archive whose metadata lists nothing and that holds nothing`() = runTest {
        stubIO(archive(metaFirst = true, photoCount = 0), usableBytes = 10_000L)

        val error = validateBackup(uri).exceptionOrNull()

        assertTrue("Expected NoBackupFiles, got $error", error is BackupValidationError.NoBackupFiles)
    }

    /** Records every stream handed out, so a test can assert how often the archive was read. */
    private class TrackedZipInputStream(bytes: ByteArray) :
        ZipInputStream(ByteArrayInputStream(bytes)) {
        var closed = false

        override fun close() {
            closed = true
            super.close()
        }
    }

    private fun stubIO(
        archive: ByteArray,
        usableBytes: Long,
        hasDirectory: Boolean = true,
    ): List<TrackedZipInputStream> {
        val opened = mutableListOf<TrackedZipInputStream>()

        val zip = mockk<IO.Zip>()
        every { zip.hasEndOfCentralDirectory(uri) } returns hasDirectory
        every { zip.openZipInput(uri) } answers {
            TrackedZipInputStream(archive).also { opened += it }
        }

        every { io.zip } returns zip
        every { io.getFileName(uri) } returns "backup.zip"
        every { io.getFileSize(uri) } returns archive.size.toLong()
        every { io.usableInternalBytes() } returns usableBytes

        return opened
    }

    private fun archive(metaFirst: Boolean, photoCount: Int): ByteArray {
        val photos = (0 until photoCount).joinToString(",") { index ->
            """
            {
              "fileName": "photo-$index.jpg",
              "importedAt": 1700000000000,
              "lastModified": null,
              "type": "JPEG",
              "size": 1000,
              "uuid": "uuid-$index"
            }
            """.trimIndent()
        }

        val meta = """
            {
              "backupVersion": 5,
              "createdAt": 1700000000000,
              "wrappedVMK": "dmtr",
              "photos": [$photos],
              "albums": [],
              "albumPhotoRefs": []
            }
        """.trimIndent()

        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            fun writeMeta() {
                zip.putNextEntry(ZipEntry(BackupMetaData.FILE_NAME))
                zip.write(meta.toByteArray())
                zip.closeEntry()
            }

            if (metaFirst) writeMeta()

            for (index in 0 until photoCount) {
                zip.putNextEntry(ZipEntry("uuid-$index.crypt"))
                zip.write(ByteArray(64))
                zip.closeEntry()
            }

            if (!metaFirst) writeMeta()
        }

        return out.toByteArray()
    }
}
