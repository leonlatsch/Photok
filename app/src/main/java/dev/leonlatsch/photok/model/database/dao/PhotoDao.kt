/*
 *   Copyright 2020-2022 Leon Latsch
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

import androidx.room.*
import dev.leonlatsch.photok.model.database.entity.Photo
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [Photo] Entity.
 * Never use directory. Use with Repository only.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@Dao
interface PhotoDao {

    /**
     * Insert one [Photo]
     *
     * @return the id of the new inserted item.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: Photo): Long

    /**
     * Delete one [Photo]
     *
     * @return the id of the deleted item.
     */
    @Delete
    suspend fun delete(photo: Photo): Int

    /**
     * Delete all [Photo] rows.
     */
    @Query("DELETE FROM photo")
    suspend fun deleteAll()

    /**
     * Get one [Photo] by [id].
     *
     * @return the photo with [id]
     */
    @Query("SELECT * FROM photo WHERE photo_uuid = :uuid")
    suspend fun get(uuid: String): Photo

    /**
     * Get all photos, ordered by imported At (desc).
     * Used for re-encrypting.
     *
     * @return all photos as [List]
     */
    @Query("SELECT * FROM photo ORDER BY importedAt DESC")
    suspend fun getAll(): List<Photo>

    @Query("SELECT * FROM photo ORDER BY importedAt DESC")
    fun observeAll(): Flow<List<Photo>>

    /**
     * Count all photos.
     */
    @Query("SELECT COUNT(*) FROM photo")
    suspend fun countAll(): Int

    // Sorted

    @Query("SELECT * FROM photo ORDER BY :sortField ASC")
    fun observeAllSortedAsc(sortField: String): Flow<List<Photo>>

    @Query("SELECT * FROM photo ORDER BY :sortField DESC")
    fun observeAllSortedDesc(sortField: String): Flow<List<Photo>>
}