/*
 *   Copyright 2020 Leon Latsch
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

package dev.leonlatsch.photok.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.leonlatsch.photok.model.database.PhotokDatabase.Companion.VERSION
import dev.leonlatsch.photok.model.database.dao.PasswordDao
import dev.leonlatsch.photok.model.database.dao.PhotoDao
import dev.leonlatsch.photok.model.database.entity.Password
import dev.leonlatsch.photok.model.database.entity.Photo

/**
 * Abstract Room Database.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@Database(
    entities = [
        Photo::class,
        Password::class
    ],
    version = VERSION,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PhotokDatabase : RoomDatabase() {

    companion object {
        const val VERSION = 1
        const val DATABASE_NAME = "photok.db"
    }

    /**
     * Get the data access object for [Photo]
     */
    abstract fun getPhotoDao(): PhotoDao

    /**
     * Get the data access object for [Photo]
     */
    abstract fun getPasswordDao(): PasswordDao
}