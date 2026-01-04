package dev.leonlatsch.photok.gallery.albums.domain

import dev.leonlatsch.photok.gallery.albums.domain.model.Album
import dev.leonlatsch.photok.gallery.albums.domain.model.AlbumPhotoRef
import dev.leonlatsch.photok.sort.domain.Sort
import dev.leonlatsch.photok.model.database.entity.Photo
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {
    fun observeAllAlbumsWithPhotos(): Flow<List<Album>>
    suspend fun getAlbums(): List<Album>
    fun observeAlbumWithPhotos(uuid: String, sort: Sort): Flow<Album>
    suspend fun getPhotosForAlbum(uuid: String): List<Photo>
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