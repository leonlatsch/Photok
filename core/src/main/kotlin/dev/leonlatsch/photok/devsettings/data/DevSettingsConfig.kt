/*
 *   Copyright 2020-2026 Leon Latsch
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DevSettingsConfig @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val preferences: SharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    val valuesFlow: Flow<Map<String, *>> = callbackFlow {
        send(preferences.all)

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, _ ->
            coroutineScope.launch { send(sharedPreferences.all) }
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            preferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    var overrideHasPro: Boolean
        get() = preferences.getBoolean(KEY_OVERRIDE_HAS_PRO, KEY_OVERRIDE_HAS_PRO_DEFAULT)
        set(value) = preferences.edit { putBoolean(KEY_OVERRIDE_HAS_PRO, value) }

    companion object {
        const val FILE_NAME = "dev.leonlatsch.photok_dev_settings_preferences"

        private const val KEY_OVERRIDE_HAS_PRO = "overrideHasPro"
        private const val KEY_OVERRIDE_HAS_PRO_DEFAULT = false
    }
}
