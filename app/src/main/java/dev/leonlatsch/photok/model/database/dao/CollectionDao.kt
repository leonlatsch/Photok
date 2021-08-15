/*
 *   Copyright 2020-2021 Leon Latsch
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
import dev.leonlatsch.photok.model.database.entity.Collection
import dev.leonlatsch.photok.model.database.relation.CollectionWithPhotos

@Dao
interface CollectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(collection: Collection): Long

    @Delete
    suspend fun delete(collection: Collection): Int

    @Query("DELETE FROM collection")
    suspend fun deleteAll()

    @Query("SELECT * FROM collection WHERE id = :collectionId")
    suspend fun get(collectionId: Int): Collection

    @Query("SELECT * FROM collection")
    suspend fun getAll(): List<Collection>

    @Transaction
    @Query("SELECT * FROM collection WHERE id = :collectionId")
    suspend fun getCollectionWithPhotos(collectionId: Int): CollectionWithPhotos
}