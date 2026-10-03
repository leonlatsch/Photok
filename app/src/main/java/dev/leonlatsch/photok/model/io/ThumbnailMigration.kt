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

import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

/**
 * Gives the gallery time to load its first thumbnails before competing for CPU.
 */
private val StartDelay = 3.seconds

/**
 * Replaces outdated thumbnails in the background while the vault is unlocked.
 * Progress is stored per photo, so it continues where it stopped after the next unlock.
 */
@Singleton
class ThumbnailMigration @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val photoRepository: PhotoRepository,
    private val regenerateThumbnail: RegenerateThumbnailUseCase,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun start() {
        scope.launch {
            sessionRepository.observe()
                .flatMapLatest { session ->
                    if (session == null) {
                        emptyFlow()
                    } else {
                        photoRepository.observeWithOutdatedThumbnail()
                            .onStart { delay(StartDelay) }
                    }
                }
                .conflate()
                .collect { photos ->
                    photos.forEach { photo ->
                        regenerateThumbnail(photo).onFailure {
                            Timber.w(it, "Could not regenerate thumbnail for ${photo.uuid}")
                        }
                    }
                }
        }
    }
}
