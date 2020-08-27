package dev.leonlatsch.photok.model.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import dev.leonlatsch.photok.model.database.entity.Photo

@Dao
interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: Photo): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(photos: List<Photo>)

    @Delete
    suspend fun delete(photo: Photo)

    @Query("SELECT * FROM photo ORDER BY importedAt DESC")
    fun getAllPhotosSortedByImportedAt(): LiveData<List<Photo>>
}