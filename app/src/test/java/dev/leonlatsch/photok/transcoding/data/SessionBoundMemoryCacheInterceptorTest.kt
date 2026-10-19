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

package dev.leonlatsch.photok.transcoding.data

import android.app.Application
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toDrawable
import coil.decode.DataSource
import coil.intercept.Interceptor
import coil.memory.MemoryCache
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.encryption.domain.crypto.KeyGen
import dev.leonlatsch.photok.encryption.domain.models.VaultSession
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class SessionBoundMemoryCacheInterceptorTest {

    private val app: Application = RuntimeEnvironment.getApplication()
    private val key = MemoryCache.Key("thumbnail#uuid")
    private val memoryCache = MemoryCache.Builder(app).maxSizePercent(0.25).build()
    private val sessionRepository = mockk<SessionRepository>()
    private val interceptor = SessionBoundMemoryCacheInterceptor(sessionRepository, memoryCache)

    @Test
    fun `result is kept while the session stays the same`() = runTest {
        val session = session()
        every { sessionRepository.get() } returns session

        val result = interceptor.intercept(chainThatCaches())

        assertTrue(result is SuccessResult)
        assertNotNull(memoryCache[key])
    }

    @Test
    fun `result is dropped when the session ends while loading`() = runTest {
        every { sessionRepository.get() } returnsMany listOf(session(), null)

        val result = interceptor.intercept(chainThatCaches())

        assertTrue(result is ErrorResult)
        assertNull(memoryCache[key])
    }

    @Test
    fun `result is dropped when the vault is unlocked again while loading`() = runTest {
        every { sessionRepository.get() } returnsMany listOf(session(), session())

        val result = interceptor.intercept(chainThatCaches())

        assertTrue(result is ErrorResult)
        assertNull(memoryCache[key])
    }

    private fun chainThatCaches(): Interceptor.Chain {
        val request = ImageRequest.Builder(app).data("thumbnail").build()
        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

        return mockk {
            every { this@mockk.request } returns request
            coEvery { proceed(request) } answers {
                memoryCache[key] = MemoryCache.Value(bitmap)
                SuccessResult(
                    drawable = bitmap.toDrawable(app.resources),
                    request = request,
                    dataSource = DataSource.DISK,
                    memoryCacheKey = key,
                )
            }
        }
    }

    private fun session() = VaultSession(KeyGen().generateVaultMasterKey())
}
