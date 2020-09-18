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

package dev.leonlatsch.photok.other

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages reading and writing with the config file.
 *
 * @since 1.0.0
 */
class PrefManager(context: Context) {

    private val fileName = "dev.leonlatsch.photok_preferences"
    private val mode = Context.MODE_PRIVATE

    private val preferences: SharedPreferences
    private val edit: SharedPreferences.Editor

    init {
        preferences = context.getSharedPreferences(fileName, mode)
        edit = preferences.edit()
    }

    fun getString(key: String, default: String?) = preferences.getString(key, default)

    fun getInt(key: String, default: Int) = preferences.getInt(key, default)

    fun getBoolean(key: String, default: Boolean) = preferences.getBoolean(key, default)

    fun putString(key: String, value: String) {
        edit.putString(key, value)
        edit.apply()
    }

    fun putInt(key: String, value: Int) {
        edit.putInt(key, value)
        edit.apply()
    }

    fun putBoolean(key: String, value: Boolean) {
        edit.putBoolean(key, value)
        edit.apply()
    }
}