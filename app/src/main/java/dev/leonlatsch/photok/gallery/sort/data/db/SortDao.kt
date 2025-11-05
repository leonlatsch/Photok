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

package dev.leonlatsch.photok.gallery.sort.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.leonlatsch.photok.gallery.sort.data.db.model.SortTable
import kotlinx.coroutines.flow.Flow

@Dao
interface SortDao {

    @Query("SELECT * FROM sort WHERE album = :album OR (album IS NULL AND :album IS NULL)")
    fun observeSort(album: String? = null): Flow<SortTable?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSortFor(sort: SortTable)

    @Query("DELETE FROM sort WHERE album = :album OR (album IS NULL AND :album IS NULL)")
    suspend fun deleteSortFor(album: String? = null)
}