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

package dev.leonlatsch.photok.model.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.sort.domain.Sort
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: Photo): Long

    @Delete
    suspend fun delete(photo: Photo): Int

    @Query("DELETE FROM photo")
    suspend fun deleteAll()

    @Query("SELECT * FROM photo WHERE photo_uuid = :uuid")
    suspend fun get(uuid: String): Photo

    @Query("SELECT * FROM photo ORDER BY importedAt DESC")
    suspend fun findAllPhotosByImportDateDesc(): List<Photo>

    @Query("SELECT * FROM photo ORDER BY importedAt DESC")
    fun observeAll(): Flow<List<Photo>>

    @Query("SELECT COUNT(*) FROM photo")
    suspend fun countAll(): Int

    fun observeAllSorted(sort: Sort): Flow<List<Photo>> {
        val query = SimpleSQLiteQuery("SELECT * FROM photo ORDER BY ${sort.field.columnName} ${sort.order.sql}")

        return observeAll(query)
    }

    @RawQuery(observedEntities = [Photo::class])
    fun observeAll(query: SupportSQLiteQuery): Flow<List<Photo>>
}
