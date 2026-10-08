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

import coil.intercept.Interceptor
import coil.memory.MemoryCache
import coil.request.ErrorResult
import coil.request.ImageResult
import coil.request.SuccessResult
import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.transcoding.di.EncryptedImageMemoryCache
import javax.inject.Inject

/**
 * Drops results whose session ended while they were loading. Clearing the memory cache on lock
 * alone misses requests that finish and write to the cache after the clear.
 */
class SessionBoundMemoryCacheInterceptor @Inject constructor(
    private val sessionRepository: SessionRepository,
    @EncryptedImageMemoryCache private val memoryCache: MemoryCache,
) : Interceptor {

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val session = sessionRepository.get()
        val result = chain.proceed(chain.request)

        if (sessionRepository.get() === session) {
            return result
        }

        if (result is SuccessResult) {
            result.memoryCacheKey?.let { memoryCache.remove(it) }
        }

        return ErrorResult(
            drawable = null,
            request = chain.request,
            throwable = IllegalStateException("Session ended while loading"),
        )
    }
}
