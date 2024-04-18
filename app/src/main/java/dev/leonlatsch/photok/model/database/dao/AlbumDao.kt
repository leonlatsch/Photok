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
interface AlbumDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(album: AlbumTable): Long

    @Delete
    suspend fun delete(album: AlbumTable): Int

    @Query("DELETE FROM albumtable")
    suspend fun deleteAll()

    @Query("SELECT * FROM albumtable WHERE album_uuid = :uuid")
    suspend fun getAlbumByUuid(uuid: String): AlbumTable

    @Transaction
    @Query("SELECT * FROM albumtable")
    fun getAllAlbumsWithPhotos(): Flow<List<AlbumWithPhotos>>

    @Transaction
    @Query("SELECT * FROM albumtable WHERE album_uuid = :uuid")
    fun getAlbumWithPhotos(uuid: String): Flow<AlbumWithPhotos>

    @Query("SELECT COUNT(*) FROM albumtable")
    suspend fun countAll(): Int

    @Query("INSERT OR IGNORE INTO album_photos_cross_ref (album_uuid, photo_uuid) VALUES (:albumId, :photoId)")
    suspend fun linkPhotoToAlbum(photoId: String, albumId: String)
}