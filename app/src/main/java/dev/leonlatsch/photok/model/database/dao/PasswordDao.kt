package dev.leonlatsch.photok.model.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import dev.leonlatsch.photok.model.database.entity.Password

/**
 * Data Access Object for [Password] Entity.
 *
 * @since 1.0.0
 */
@Dao
interface PasswordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(password: Password)

    @Delete
    suspend fun delete(password: Password)

    @Update
    suspend fun update(password: Password)

    /**
     * Load the only password with id = 0
     */
    @Query("SELECT * FROM password WHERE id = 0")
    suspend fun getPassword(): Password?
}