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

package dev.leonlatsch.photok.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.leonlatsch.photok.model.database.dao.AlbumDao
import dev.leonlatsch.photok.model.database.dao.PhotoDao
import dev.leonlatsch.photok.model.database.entity.Album
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.ref.AlbumPhotosCrossRef

private const val DATABASE_VERSION = 2
const val DATABASE_NAME = "photok.db"

/**
 * Abstract Room Database.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@Database(
    entities = [
        Photo::class,
        Album::class,
        AlbumPhotosCrossRef::class
    ],
    version = DATABASE_VERSION,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PhotokDatabase : RoomDatabase() {

    /**
     * Get the data access object for [Photo]
     */
    abstract fun getPhotoDao(): PhotoDao

    abstract fun getAlbumDao(): AlbumDao
}