package dev.leonlatsch.photok.model.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: Photo)

    @Delete
    suspend fun deletePhoto(photo: Photo)

    @Query("SELECT * FROM photo ORDER BY importedAt DESC")
    fun getAllPhotosSortedByImportedAt(): LiveData<List<Photo>>
}