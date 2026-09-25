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

package dev.leonlatsch.photok.backup.data

import com.google.gson.GsonBuilder
import dev.leonlatsch.photok.backup.domain.BackupValidationError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * The version field decides which shape the rest of meta.json has, so these pin down every value
 * the field can realistically carry, including the ones written by an app older or newer than
 * this one.
 */
class ReadBackupMetadataUseCaseTest {

    private val readBackupMetadata = ReadBackupMetadataUseCase(GsonBuilder().create())

    @Test
    fun `reads a current backup`() = runTest {
        val metaData = read(
            """
            {
              "backupVersion": 5,
              "createdAt": 1700000000000,
              "wrappedVMK": "dmtr",
              "photos": [],
              "albums": [],
              "albumPhotoRefs": []
            }
            """
        )

        assertTrue(metaData is BackupMetaData.V5)
        assertEquals(5, metaData.backupVersion)
    }

    @Test
    fun `treats a backup without a version as V1`() = runTest {
        val metaData = read("""{ "password": "hash", "photos": [] }""")

        assertTrue(metaData is BackupMetaData.V1)
        assertEquals(1, metaData.backupVersion)
    }

    @Test
    fun `fills in the albums a pre album backup does not have`() = runTest {
        val metaData = read("""{ "backupVersion": 2, "password": "hash", "photos": [] }""")

        assertEquals(emptyList<AlbumBackup>(), metaData.albums)
        assertEquals(emptyList<AlbumPhotoRefBackup>(), metaData.albumPhotoRefs)
    }

    @Test
    fun `reports a backup from a newer app as unsupported`() = runTest {
        val error = runCatching {
            read("""{ "backupVersion": 6, "photos": [] }""")
        }.exceptionOrNull()

        assertTrue(
            "Expected UnsupportedVersion, got $error",
            error is BackupValidationError.UnsupportedVersion,
        )
        assertEquals(6, (error as BackupValidationError.UnsupportedVersion).version)
    }

    private suspend fun read(json: String): BackupMetaData {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            zip.putNextEntry(ZipEntry(BackupMetaData.FILE_NAME))
            zip.write(json.trimIndent().toByteArray())
            zip.closeEntry()
        }

        return ZipInputStream(ByteArrayInputStream(out.toByteArray())).use { zip ->
            zip.nextEntry
            readBackupMetadata(zip)
        }
    }
}
