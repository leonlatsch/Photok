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
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import dev.leonlatsch.photok.model.database.entity.AlbumTable
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.ref.AlbumPhotoCrossRefTable
import dev.leonlatsch.photok.model.database.ref.AlbumWithPhotos
import dev.leonlatsch.photok.sort.domain.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.intellij.lang.annotations.Language


@Dao
abstract class AlbumDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(album: AlbumTable): Long

    @Delete
    abstract suspend fun delete(album: AlbumTable): Int

    @Query("DELETE FROM album")
    abstract suspend fun deleteAll()



    @Query("SELECT * FROM album")
    abstract suspend fun getAllAlbums(): List<AlbumTable>

    @Query("SELECT * FROM album")
    abstract fun observeAllAlbums(): Flow<List<AlbumTable>>


    @Query("SELECT * FROM album WHERE album_uuid = :uuid")
    abstract fun observeAlbum(uuid: String): Flow<AlbumTable>

    @Query("SELECT * FROM album WHERE album_uuid = :uuid")
    abstract suspend fun getAlbum(uuid: String): AlbumTable?


    @Query("SELECT photo_uuid, linked_at FROM album_photos_cross_ref WHERE photo_uuid in (:photoUUIDs)")
    abstract suspend fun getLinkedAtFor(
        photoUUIDs: List<String>
    ): Map<@MapColumn(columnName = "photo_uuid") String, @MapColumn(columnName = "linked_at") Long>

    @Query("INSERT OR IGNORE INTO album_photos_cross_ref (album_uuid, photo_uuid, linked_at) VALUES (:albumId, :photoId, :linkedAt)")
    abstract suspend fun link(photoId: String, albumId: String, linkedAt: Long)

    @Transaction
    open suspend fun link(photoUUIDs: List<String>, albumUUID: String) {
        photoUUIDs.forEach {
            link(it, albumUUID, System.currentTimeMillis())
        }
    }

    @Query("DELETE FROM album_photos_cross_ref WHERE album_uuid = :albumUUID AND photo_uuid IN (:photoUUIDs)")
    abstract suspend fun unlink(photoUUIDs: List<String>, albumUUID: String)

    @Query("DELETE FROM album_photos_cross_ref WHERE photo_uuid = :photoUUID")
    abstract suspend fun unlink(photoUUID: String)

    @Query("DELETE FROM album_photos_cross_ref")
    abstract suspend fun unlinkAll()

    @Query("UPDATE album SET name = :newName WHERE album_uuid = :albumUUID")
    abstract suspend fun renameAlbum(albumUUID: String, newName: String)

    @Query("DELETE FROM album_photos_cross_ref WHERE album_uuid = :albumId")
    abstract suspend fun removeAllPhotosFromAlbum(albumId: String)

    @Transaction
    open suspend fun unlinkAndDeleteAlbum(album: AlbumTable): Int {
        removeAllPhotosFromAlbum(album.uuid)
        return delete(album)
    }

    @Query("SELECT * FROM album_photos_cross_ref")
    abstract suspend fun getAllAlbumPhotoRefs(): List<AlbumPhotoCrossRefTable>

    // Sorting

    open fun observeAlbumWithPhotos(uuid: String, sort: Sort): Flow<AlbumWithPhotos> {
        val query = createSortedPhotosQuery(uuid, sort)

        return combine(
            observeAlbum(uuid),
            observePhotosForAlbum(query)
        ) { album, photos ->
            AlbumWithPhotos(album, photos)
        }
    }

    @RawQuery(observedEntities = [Photo::class, AlbumPhotoCrossRefTable::class])
    abstract fun observePhotosForAlbum(query: SupportSQLiteQuery): Flow<List<Photo>>

    open suspend fun getPhotosForAlbum(uuid: String, sort: Sort): List<Photo> {
        val query = createSortedPhotosQuery(uuid, sort)
        return getPhotosForAlbum(query)
    }

    @RawQuery
    abstract suspend fun getPhotosForAlbum(query: SupportSQLiteQuery): List<Photo>

    private fun createSortedPhotosQuery(album: String, sort: Sort): SupportSQLiteQuery {
        @Language("roomsql")
        val sql = """
            SELECT p.*
            FROM ${Photo.TABLE_NAME} p
            INNER JOIN ${AlbumPhotoCrossRefTable.TABLE_NAME} ref ON p.photo_uuid = ref.photo_uuid
            WHERE ref.album_uuid = ?
            ORDER BY ${sort.field.columnName} ${sort.order.sql}
        """.trimIndent()

        return SimpleSQLiteQuery(sql, arrayOf(album))
    }
}