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
import dev.leonlatsch.photok.gallery.albums.domain.model.AlbumPhotoRef
import dev.leonlatsch.photok.gallery.albums.toData
import dev.leonlatsch.photok.gallery.albums.toDomain
import dev.leonlatsch.photok.sort.domain.Sort
import dev.leonlatsch.photok.sort.domain.SortRepository
import dev.leonlatsch.photok.sort.ui.SortConfig
import dev.leonlatsch.photok.model.database.dao.AlbumDao
import dev.leonlatsch.photok.model.database.entity.Photo
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class AlbumRepositoryImpl @Inject constructor(
    private val albumDao: AlbumDao,
    private val sortRepository: SortRepository,
) : AlbumRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeAlbumsWithCovers(): Flow<List<Album>> {
        return sortRepository.observeSortsForAlbums().flatMapLatest { sorts ->
            albumDao.observeAllAlbums().map { albums ->
                albums.map { album ->
                    val cover = albumDao.getCoverForAlbum(album.uuid, sorts[album.uuid] ?: SortConfig.Album.default)
                    album.toDomain().copy(files = listOf(cover))
                }
            }
        }
    }

    override suspend fun getAlbums(): List<Album> = albumDao.getAllAlbums()
        .map { album -> album.toDomain() }

    override fun observeAlbumWithPhotos(uuid: String, sort: Sort): Flow<Album> =
        albumDao.observeAlbumWithPhotos(uuid, sort)
            .map { it.toDomain() }

    override suspend fun getPhotosForAlbum(uuid: String): List<Photo> = withContext(IO) {
        val sort = sortRepository.getSortForAlbum(uuid) ?: SortConfig.Album.default

        albumDao.getPhotosForAlbum(uuid, sort)
    }

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

    override suspend fun deleteAll() {
        albumDao.deleteAll()
    }

    override suspend fun link(photoUUIDs: List<String>, albumUUID: String) {
        albumDao.link(photoUUIDs, albumUUID)
    }

    override suspend fun link(ref: AlbumPhotoRef) {
        with(ref) {
            albumDao.link(
                photoId = photoUUID,
                albumId = albumUUID,
                linkedAt = linkedAt,
            )
        }
    }

    override suspend fun unlink(photoUUIDs: List<String>, uuid: String) {
        albumDao.unlink(photoUUIDs, uuid)
    }

    override suspend fun unlinkAll() {
        albumDao.unlinkAll()
    }

    override suspend fun rename(albumUUID: String, newName: String) {
        albumDao.renameAlbum(albumUUID = albumUUID, newName = newName)
    }

    override suspend fun getAllAlbumPhotoLinks(): List<AlbumPhotoRef> =
        albumDao.getAllAlbumPhotoRefs().map { ref ->
            ref.toDomain()
        }
}