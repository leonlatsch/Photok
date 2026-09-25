/*
 *   Copyright 2020-2026 Leon Latsch
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

package dev.leonlatsch.photok.backup.ui

import android.net.Uri
import dev.leonlatsch.photok.backup.domain.BackupStrategy
import dev.leonlatsch.photok.backup.domain.BackupStrategyImpl
import dev.leonlatsch.photok.backup.domain.LegacyBackupStrategyImpl
import dev.leonlatsch.photok.encryption.domain.VaultProtectionRepository
import dev.leonlatsch.photok.encryption.domain.models.Algorithm
import dev.leonlatsch.photok.encryption.domain.models.Kdf
import dev.leonlatsch.photok.encryption.domain.models.VaultProtection
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionParams
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionType
import dev.leonlatsch.photok.io.IO
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.uicomponnets.base.processdialogs.ProcessState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.ByteArrayOutputStream
import java.util.zip.ZipOutputStream

/**
 * The meta file holds the wrapped vault master key of a V5 backup. A backup that ends before it is
 * written can never be decrypted again, so these tests pin down that it is written first and that
 * a backup which lost that file does not survive.
 *
 * A single photo that failed is a different matter: the archive still holds the rest of the vault
 * and meta.json still lists the photo, so restore reports it as missing. Throwing the whole file
 * away over it would be worse, and these tests pin that down too.
 */
@RunWith(RobolectricTestRunner::class)
class BackupViewModelTest {

    private val backupUri = Uri.parse("content://test/backup.zip")

    private val photos = listOf(photo("one.jpg"), photo("two.jpg"))

    private val mockPhotoRepository = mockk<PhotoRepository>()
    private val mockIO = mockk<IO>(relaxed = true)
    private val mockStrategy = mockk<BackupStrategyImpl>(relaxed = true)
    private val mockLegacyStrategy = mockk<LegacyBackupStrategyImpl>(relaxed = true)
    private val mockProtectionRepository = mockk<VaultProtectionRepository>()

    private lateinit var viewModel: BackupViewModel

    @Before
    fun setup() {
        coEvery { mockPhotoRepository.findAllPhotosByImportDateDesc() } returns photos
        coEvery { mockProtectionRepository.getProtection(VaultProtectionType.Password) } returns
            passwordProtection()

        every { mockIO.zip.openZipOutput(backupUri) } returns
            ZipOutputStream(ByteArrayOutputStream())

        coEvery { mockStrategy.createMetaFileInBackup(any()) } returns Result.success(Unit)
        coEvery { mockStrategy.writePhotoToBackup(any(), any()) } returns Result.success(Unit)

        viewModel = BackupViewModel(
            app = RuntimeEnvironment.getApplication(),
            photoRepository = mockPhotoRepository,
            io = mockIO,
            defaultBackupStrategy = mockStrategy,
            legacyBackupStrategy = mockLegacyStrategy,
            vaultProtectionRepository = mockProtectionRepository,
        ).apply {
            uri = backupUri
            strategyName = BackupStrategy.Name.Default
        }
    }

    @Test
    fun `writes the meta file before the first photo`() = runTest {
        runBackup()

        coVerifyOrder {
            mockStrategy.createMetaFileInBackup(any())
            mockStrategy.writePhotoToBackup(photos[0], any())
            mockStrategy.writePhotoToBackup(photos[1], any())
        }
    }

    @Test
    fun `keeps the backup when every photo was written`() = runTest {
        runBackup()

        coVerify(exactly = 0) { mockIO.deleteFile(any()) }
    }

    @Test
    fun `keeps the backup when a single photo fails`() = runTest {
        coEvery { mockStrategy.writePhotoToBackup(photos[1], any()) } returns
            Result.failure(IllegalStateException("Input stream missing for photo"))

        runBackup()

        coVerify(exactly = 0) { mockIO.deleteFile(any()) }
        assertTrue("A failed photo must still raise the warning", viewModel.failuresOccurred)
    }

    @Test
    fun `deletes the backup when the meta file fails`() = runTest {
        coEvery { mockStrategy.createMetaFileInBackup(any()) } returns
            Result.failure(IllegalStateException("Could not write meta file"))

        runBackup()

        coVerify(exactly = 0) { mockStrategy.writePhotoToBackup(any(), any()) }
        coVerify { mockIO.deleteFile(backupUri) }
    }

    @Test
    fun `deletes the backup when the vault has no password protection`() = runTest {
        coEvery { mockProtectionRepository.getProtection(VaultProtectionType.Password) } returns null

        runBackup()

        coVerify(exactly = 0) { mockStrategy.writePhotoToBackup(any(), any()) }
        coVerify { mockIO.deleteFile(backupUri) }
    }

    @Test
    fun `deletes nothing when the backup file can not be opened`() = runTest {
        every { mockIO.zip.openZipOutput(backupUri) } returns null

        runBackup()

        coVerify(exactly = 0) { mockStrategy.createMetaFileInBackup(any()) }
        coVerify(exactly = 0) { mockStrategy.writePhotoToBackup(any(), any()) }
    }

    @Test
    fun `deletes the backup when it is canceled halfway`() = runTest {
        viewModel.preProcess()
        viewModel.processItem(photos[0])
        viewModel.cancel()
        viewModel.postProcess()

        coVerify { mockIO.deleteFile(backupUri) }
    }

    /** Mirrors what [BackupViewModel.runProcessing] does, the process loop is private. */
    private suspend fun runBackup() {
        viewModel.preProcess()

        for (item in viewModel.items) {
            if (viewModel.processState == ProcessState.ABORTED) break
            viewModel.processItem(item)
        }

        viewModel.postProcess()
    }

    private fun photo(fileName: String) = Photo(
        fileName = fileName,
        importedAt = 0,
        type = PhotoType.JPEG,
        size = 1_000L,
        lastModified = null,
        uuid = "uuid-$fileName",
    )

    private fun passwordProtection() = VaultProtection(
        id = "protection",
        type = VaultProtectionType.Password,
        wrappedVMK = ByteArray(32),
        params = VaultProtectionParams(
            salt = "salt",
            iv = "iv",
            kdf = Kdf.PBKDF2WithHmacSHA256,
            kdfIterations = 100_000,
            algorithm = Algorithm.AesCbcPkcs7Padding,
            keySize = 256,
        ),
    )
}
