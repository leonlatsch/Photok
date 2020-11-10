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

package dev.leonlatsch.photok.ui.backup

import com.google.gson.annotations.Expose
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.model.database.entity.Photo

/**
 * Model for meta.json in backups.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
data class BackupDetails(
    @Expose val password: String,
    @Expose val photos: List<Photo>,
    @Expose val createdAt: Long = System.currentTimeMillis(),
    @Expose val version: String = BuildConfig.VERSION_NAME
) {
    companion object {
        const val FILE_NAME = "meta.json"
    }
}