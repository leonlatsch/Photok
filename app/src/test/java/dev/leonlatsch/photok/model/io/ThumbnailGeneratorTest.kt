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
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.decode.DataSource
import coil.request.ImageRequest
import coil.request.SuccessResult
import dev.leonlatsch.photok.encryption.domain.crypto.CbcCryptoEngine
import dev.leonlatsch.photok.encryption.domain.crypto.KeyGen
import dev.leonlatsch.photok.encryption.domain.models.VaultSession
import dev.leonlatsch.photok.io.VaultFileStorage
import dev.leonlatsch.photok.model.database.entity.PhotoType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

@RunWith(RobolectricTestRunner::class)
class ThumbnailGeneratorTest {

    private val app: Application = RuntimeEnvironment.getApplication()

    private val vaultFileStorage = VaultFileStorage(
        sessionRepository = mockk { every { get() } returns VaultSession(KeyGen().generateVaultMasterKey()) },
        cryptoEngine = CbcCryptoEngine(),
        app = app,
    )

    private val encryptedImageLoader = mockk<ImageLoader> {
        coEvery { execute(any()) } coAnswers {
            delay(100)
            SuccessResult(
                drawable = BitmapDrawable(app.resources, Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)),
                request = firstArg<ImageRequest>(),
                dataSource = DataSource.MEMORY,
            )
        }
    }

    private val generator = ThumbnailGenerator(
        context = app,
        encryptedImageLoader = { encryptedImageLoader },
        imageLoader = mockk(),
        vaultFileStorage = vaultFileStorage,
    )

    @After
    fun tearDown() {
        File(app.cacheDir, ThumbnailFiles.DIR).deleteRecursively()
    }

    @Test
    fun `concurrent requests for the same photo create its thumbnail once`() = runTest {
        val results = List(3) {
            async { generator.createFromVault("uuid", PhotoType.JPEG).getOrThrow() }
        }.awaitAll()

        coVerify(exactly = 1) { encryptedImageLoader.execute(any()) }
        assertEquals(1, results.count { it is CreatedThumbnail.New })
        assertEquals(2, results.count { it == CreatedThumbnail.Existing })
        assertTrue(vaultFileStorage.cacheFileExists(ThumbnailFiles.path("uuid")))
    }

    @Test
    fun `an existing thumbnail is not created again`() = runTest {
        vaultFileStorage.openEncryptedCacheOutput(ThumbnailFiles.path("uuid"))!!.use { it.write(byteArrayOf(1)) }

        val result = generator.createFromVault("uuid", PhotoType.JPEG).getOrThrow()

        assertEquals(CreatedThumbnail.Existing, result)
        coVerify(exactly = 0) { encryptedImageLoader.execute(any()) }
    }
}
