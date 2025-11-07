/*
 *   Copyright 2020-2024 Leon Latsch
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

package dev.leonlatsch.photok.gallery.albums.domain

import dev.leonlatsch.photok.gallery.albums.domain.model.Album
import dev.leonlatsch.photok.gallery.albums.domain.model.AlbumPhotoRef
import dev.leonlatsch.photok.gallery.sort.domain.Sort
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {
    fun observeAlbumsWithPhotos(): Flow<List<Album>>
    suspend fun getAlbums(): List<Album>
    fun observeAlbumWithPhotos(uuid: String, sort: Sort): Flow<Album>
    suspend fun getAlbumWithPhotos(uuid: String): Album
    suspend fun createAlbum(album: Album): Result<Album>
    suspend fun deleteAlbum(album: Album): Result<Unit>
    suspend fun deleteAll()

    suspend fun link(photoUUIDs: List<String>, albumUUID: String)
    suspend fun link(ref: AlbumPhotoRef)
    suspend fun unlink(photoUUIDs: List<String>, uuid: String)
    suspend fun unlinkAll()
    suspend fun rename(albumUUID: String, newName: String)
    suspend fun getAllAlbumPhotoLinks(): List<AlbumPhotoRef>
}