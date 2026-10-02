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

package dev.leonlatsch.photok.gallery.domain

import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.gallery.albums.domain.model.Album
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.sort.domain.SortConfig
import dev.leonlatsch.photok.sort.domain.SortRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps the sorted photo lists of the gallery and albums in memory, so the image viewer can show them instantly.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class PhotoListCache @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val albumRepository: AlbumRepository,
    private val sortRepository: SortRepository,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val galleryPhotos: StateFlow<List<Photo>> = sortRepository
        .observeSortFor(albumUuid = null, default = SortConfig.Gallery.default)
        .flatMapLatest { sort -> photoRepository.observeAll(sort) }
        .stateIn(scope, SharingStarted.WhileSubscribed(), emptyList())

    private val albums = ConcurrentHashMap<String, StateFlow<Album>>()

    fun album(albumUuid: String): StateFlow<Album> = albums.computeIfAbsent(albumUuid) {
        sortRepository
            .observeSortFor(albumUuid = albumUuid, default = SortConfig.Album.default)
            .flatMapLatest { sort -> albumRepository.observeAlbumWithPhotos(albumUuid, sort) }
            .stateIn(scope, SharingStarted.WhileSubscribed(), Album.Placeholder)
    }
}
