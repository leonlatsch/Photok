/*
 *   Copyright 2020-2023 Leon Latsch
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

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.leonlatsch.photok.other.getFileHash


val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.beginTransaction()

        try {
            database.execSQL("ALTER TABLE `photo` ADD COLUMN hash INTEGER;")

            // get photo one by one and calculate hash
            val cursor = database.query("SELECT * FROM photo")
            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                // TODO: decrypt file
                // TODO: calculate hash
                // val hash = getFileHash(app.contextResolver)
                val hash = 0L
                database.execSQL("UPDATE photo SET hash = $hash WHERE id = $id")
            }

            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }
}