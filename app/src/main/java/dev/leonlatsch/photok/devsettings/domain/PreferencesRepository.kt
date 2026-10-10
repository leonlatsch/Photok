/*
 *   Copyright 2020–2026 Leon Latsch
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

package dev.leonlatsch.photok.devsettings.domain

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun observeFiles(): Flow<List<PreferenceFile>>

    fun putString(file: String, key: String, value: String)

    fun putInt(file: String, key: String, value: Int)

    fun putLong(file: String, key: String, value: Long)

    fun putFloat(file: String, key: String, value: Float)

    fun putBoolean(file: String, key: String, value: Boolean)

    fun remove(file: String, key: String)
}
