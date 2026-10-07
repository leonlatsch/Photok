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

package dev.leonlatsch.photok.model.io

import android.app.Application
import dev.leonlatsch.photok.encryption.domain.crypto.CbcCryptoEngine
import dev.leonlatsch.photok.encryption.domain.crypto.KeyGen
import dev.leonlatsch.photok.encryption.domain.models.VaultSession
import dev.leonlatsch.photok.io.VaultCacheStorage
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

@RunWith(RobolectricTestRunner::class)
class ThumbnailMaintainerTest {

    private val app: Application = RuntimeEnvironment.getApplication()

    private val vaultCacheStorage = VaultCacheStorage(
        sessionRepository = mockk { every { get() } returns VaultSession(KeyGen().generateVaultMasterKey()) },
        cryptoEngine = CbcCryptoEngine(),
        app = app,
    )

    private val created = photo("created")
    private val failing = photo("failing")

    private val photoRepository = mockk<PhotoRepository>()

    private val thumbnailGenerator = mockk<ThumbnailGenerator> {
        coEvery { createFromVault(created.uuid, any()) } answers {
            writeCacheFile(ThumbnailFiles.path(created.uuid))
            Result.success(CreatedThumbnail.Existing)
        }
        coEvery { createFromVault(failing.uuid, any()) } returns Result.failure(Exception("broken"))
    }

    private val maintenance = ThumbnailMaintainer(
        sessionRepository = mockk(),
        photoRepository = photoRepository,
        vaultCacheStorage = vaultCacheStorage,
        thumbnailGenerator = thumbnailGenerator,
    )

    @After
    fun tearDown() {
        File(app.cacheDir, ThumbnailFiles.DIR).deleteRecursively()
    }

    @Test
    fun `outdated thumbnail versions are deleted`() = runTest {
        coEvery { photoRepository.findAllPhotosByImportDateDesc() } returns emptyList()
        writeCacheFile("${ThumbnailFiles.DIR}/v0/old.jpg")

        maintenance.run()

        assertFalse(vaultCacheStorage.encryptedFileExists("${ThumbnailFiles.DIR}/v0"))
    }

    @Test
    fun `dead thumbnails are deleted, unless they are newer than the pass`() = runTest {
        coEvery { photoRepository.findAllPhotosByImportDateDesc() } returns emptyList()
        val dead = writeCacheFile(ThumbnailFiles.path("dead"))
        dead.setLastModified(System.currentTimeMillis() - 60_000)
        val importing = writeCacheFile(ThumbnailFiles.path("importing"))
        importing.setLastModified(System.currentTimeMillis() + 60_000)

        maintenance.run()

        assertFalse(dead.exists())
        assertTrue(importing.exists())
    }

    @Test
    fun `a failing thumbnail does not stop the others`() = runTest {
        coEvery { photoRepository.findAllPhotosByImportDateDesc() } returns listOf(failing, created)

        maintenance.run()

        assertTrue(vaultCacheStorage.encryptedFileExists(ThumbnailFiles.path(created.uuid)))
    }

    private fun writeCacheFile(path: String): File {
        vaultCacheStorage.openEncryptedOutput(path)!!.use { it.write(byteArrayOf(1)) }
        return File(app.cacheDir, path)
    }

    private fun photo(uuid: String) = Photo(
        fileName = "$uuid.jpg",
        importedAt = 0L,
        type = PhotoType.JPEG,
        lastModified = null,
        uuid = uuid,
    )
}
