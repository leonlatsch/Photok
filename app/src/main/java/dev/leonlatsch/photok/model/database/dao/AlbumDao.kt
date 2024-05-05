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

package dev.leonlatsch.photok.model.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.leonlatsch.photok.model.database.entity.AlbumTable
import dev.leonlatsch.photok.model.database.ref.AlbumWithPhotos
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AlbumDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(album: AlbumTable): Long

    @Delete
    abstract suspend fun delete(album: AlbumTable): Int

    @Query("DELETE FROM album")
    abstract suspend fun deleteAll()

    @Transaction
    @Query("SELECT * FROM album")
    abstract fun getAllAlbumsWithPhotos(): Flow<List<AlbumWithPhotos>>

    @Transaction
    @Query("SELECT * FROM album WHERE album_uuid = :uuid")
    abstract fun getAlbumWithPhotos(uuid: String): Flow<AlbumWithPhotos>

    @Query("SELECT photo_uuid FROM album_photos_cross_ref WHERE album_uuid = :albumUUID ORDER BY sortingId DESC")
    abstract suspend fun getAllPhotoIdsFor(albumUUID: String): List<String>

    @Query("INSERT OR IGNORE INTO album_photos_cross_ref (album_uuid, photo_uuid) VALUES (:albumId, :photoId)")
    abstract suspend fun link(photoId: String, albumId: String)

    @Transaction
    open suspend fun link(photoUUIDs: List<String>, albumUUID: String) {
        photoUUIDs.forEach {
            link(it, albumUUID)
        }
    }

    @Query("DELETE FROM album_photos_cross_ref WHERE album_uuid = :albumUUID AND photo_uuid IN (:photoUUIDs)")
    abstract suspend fun unlink(photoUUIDs: List<String>, albumUUID: String)

    @Query("DELETE FROM album_photos_cross_ref WHERE photo_uuid = :photoUUID")
    abstract suspend fun unlink(photoUUID: String)

    @Query("DELETE FROM album_photos_cross_ref WHERE album_uuid = :albumId")
    abstract suspend fun removeAllPhotosFromAlbum(albumId: String)

    @Transaction
    open suspend fun unlinkAndDeleteAlbum(album: AlbumTable): Int {
        removeAllPhotosFromAlbum(album.uuid)
        return delete(album)
    }
}