package dev.leonlatsch.photok.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.leonlatsch.photok.model.database.PhotokDatabase.Companion.VERSION
import dev.leonlatsch.photok.model.database.dao.PasswordDao
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.dao.PhotoDao
import dev.leonlatsch.photok.model.database.entity.Password

@Database(
    entities = [
        Photo::class,
        Password::class
    ],
    version = VERSION,
    exportSchema = false
)
abstract class PhotokDatabase : RoomDatabase() {

    companion object {
        const val VERSION = 1
        const val DATABASE_NAME = "photok.db"
    }

    abstract fun getPhotoDao(): PhotoDao

    abstract fun getPasswordDao(): PasswordDao
}