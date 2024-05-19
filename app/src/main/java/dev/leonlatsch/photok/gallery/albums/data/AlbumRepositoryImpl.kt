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

package dev.leonlatsch.photok.gallery.albums.data

import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.gallery.albums.domain.model.Album
import dev.leonlatsch.photok.gallery.albums.toData
import dev.leonlatsch.photok.gallery.albums.toDomain
import dev.leonlatsch.photok.model.database.dao.AlbumDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class AlbumRepositoryImpl @Inject constructor(
    private val albumDao: AlbumDao
) : AlbumRepository {

    override fun observeAlbums(): Flow<List<Album>> =
        albumDao.getAllAlbumsWithPhotos()
            .map { albums -> albums.map { it.toDomain() } }
            .map { albums -> albums.map { album -> album.sortPhotos() } }

    override fun getAlbum(uuid: String): Flow<Album> =
        albumDao.getAlbumWithPhotos(uuid)
            .map { it.toDomain() }
            .map { album -> album.sortPhotos() }

    override suspend fun createAlbum(album: Album): Result<Album> =
        when (albumDao.insert(album.toData())) {
            -1L -> Result.failure(IOException())
            else -> Result.success(album.copy())
        }

    override suspend fun deleteAlbum(album: Album): Result<Unit> =
        when (albumDao.unlinkAndDeleteAlbum(album.toData())) {
            -1 -> Result.failure(IOException())
            else -> Result.success(Unit)
        }

    override suspend fun link(photoUUIDs: List<String>, albumUUID: String) {
        albumDao.link(photoUUIDs, albumUUID)
    }

    override suspend fun unlink(photoUUIDs: List<String>, uuid: String) {
        albumDao.unlink(photoUUIDs, uuid)
    }

    override suspend fun getAllPhotoIdsFor(albumUUID: String): List<String> {
        return albumDao.getAllPhotoIdsFor(albumUUID)
    }

    private suspend fun Album.sortPhotos(): Album {
        val linkedAt = albumDao.getLinkedAtFor(files.map { it.uuid })

        return this.copy(
            files = files.sortedByDescending { photo -> linkedAt[photo.uuid] }
        )
    }
}