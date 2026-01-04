


package dev.leonlatsch.photok.sort.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.leonlatsch.photok.sort.data.db.model.SortTable
import kotlinx.coroutines.flow.Flow

@Dao
interface SortDao {

    @Query("SELECT * FROM sort WHERE album_uuid = :album OR (album_uuid IS NULL AND :album IS NULL)")
    fun observeSort(album: String? = null): Flow<SortTable?>

    @Query("SELECT * FROM sort WHERE album_uuid = :album OR (album_uuid IS NULL AND :album IS NULL)")
    fun getSortForAlbum(album: String?): SortTable?

    @Query("SELECT * FROM sort WHERE album_uuid IS NOT NULL")
    fun observeSortsForAlbums(): Flow<List<SortTable>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSortFor(sort: SortTable)

    @Query("DELETE FROM sort WHERE album_uuid = :album OR (album_uuid IS NULL AND :album IS NULL)")
    suspend fun deleteSortFor(album: String? = null)
}

package dev.leonlatsch.photok.sort.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.leonlatsch.photok.sort.data.db.model.SortTable
import kotlinx.coroutines.flow.Flow

@Dao
interface SortDao {

    @Query("SELECT * FROM sort WHERE album_uuid = :album OR (album_uuid IS NULL AND :album IS NULL)")
    fun observeSort(album: String? = null): Flow<SortTable?>

    @Query("SELECT * FROM sort WHERE album_uuid = :album OR (album_uuid IS NULL AND :album IS NULL)")
    fun getSortForAlbum(album: String?): SortTable?

    @Query("SELECT * FROM sort WHERE album_uuid IS NOT NULL")
    fun observeSortsForAlbums(): Flow<List<SortTable>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSortFor(sort: SortTable)

    @Query("DELETE FROM sort WHERE album_uuid = :album OR (album_uuid IS NULL AND :album IS NULL)")
    suspend fun deleteSortFor(album: String? = null)
}