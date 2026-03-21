/*
 *   Copyright 2020-2026 Leon Latsch
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

package dev.leonlatsch.photok.vaults.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface VaultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vault: VaultTable)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(vaultTable: VaultTable)

    @Query("SELECT * FROM vault")
    suspend fun findAll(): List<VaultTable>

    @Query("SELECT * FROM vault WHERE vault_uuid = :uuid")
    suspend fun find(uuid: String): VaultTable?

    @Query("SELECT COUNT(*) FROM vault")
    suspend fun countVaults(): Int

    @Query("DELETE FROM vault")
    suspend fun deleteAll()
}