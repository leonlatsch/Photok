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

package dev.leonlatsch.photok.devsettings.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.devsettings.domain.PreferenceEntry
import dev.leonlatsch.photok.devsettings.domain.PreferenceFile
import dev.leonlatsch.photok.devsettings.domain.PreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : PreferencesRepository {

    override fun observeFiles(): Flow<List<PreferenceFile>> = callbackFlow {
        val preferencesByName = listFileNames().associateWith { open(it) }

        fun readAll() = preferencesByName.map { (name, preferences) ->
            PreferenceFile(
                name = name,
                entries = preferences.all
                    .mapNotNull { (key, value) -> toEntry(key, value) }
                    .sortedBy { it.key },
            )
        }

        send(readAll())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(readAll())
        }
        preferencesByName.values.forEach { it.registerOnSharedPreferenceChangeListener(listener) }

        awaitClose {
            preferencesByName.values.forEach { it.unregisterOnSharedPreferenceChangeListener(listener) }
        }
    }.flowOn(Dispatchers.IO)

    override fun putString(file: String, key: String, value: String) {
        open(file).edit { putString(key, value) }
    }

    override fun putInt(file: String, key: String, value: Int) {
        open(file).edit { putInt(key, value) }
    }

    override fun putLong(file: String, key: String, value: Long) {
        open(file).edit { putLong(key, value) }
    }

    override fun putFloat(file: String, key: String, value: Float) {
        open(file).edit { putFloat(key, value) }
    }

    override fun putBoolean(file: String, key: String, value: Boolean) {
        open(file).edit { putBoolean(key, value) }
    }

    override fun remove(file: String, key: String) {
        open(file).edit { remove(key) }
    }

    private fun listFileNames(): List<String> =
        File(context.dataDir, "shared_prefs")
            .listFiles { file -> file.extension == "xml" }
            .orEmpty()
            .map { it.nameWithoutExtension }
            .sorted()

    private fun open(name: String): SharedPreferences =
        context.getSharedPreferences(name, Context.MODE_PRIVATE)

    @Suppress("UNCHECKED_CAST")
    private fun toEntry(key: String, value: Any?): PreferenceEntry? = when (value) {
        is String -> PreferenceEntry.StringEntry(key, value)
        is Int -> PreferenceEntry.IntEntry(key, value)
        is Long -> PreferenceEntry.LongEntry(key, value)
        is Float -> PreferenceEntry.FloatEntry(key, value)
        is Boolean -> PreferenceEntry.BooleanEntry(key, value)
        is Set<*> -> PreferenceEntry.StringSetEntry(key, value as Set<String>)
        else -> null
    }
}
