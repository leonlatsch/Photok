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

package dev.leonlatsch.photok.settings

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages reading and writing with the config file.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class Config(context: Context) {

    private val preferences: SharedPreferences

    init {
        preferences = context.getSharedPreferences(FILE_NAME, MODE)
    }

    /**
     * Get a string value from the preferences.
     */
    fun getString(key: String, default: String?) = preferences.getString(key, default)

    /**
     * Get an int value from the preferences.
     */
    fun getInt(key: String, default: Int) = preferences.getInt(key, default)

    /**
     * Gets an int value which is stored as a string.
     * Like the ones created by DropDownPreference.
     */
    fun getIntFromString(key: String, default: Int): Int {
        val origValue = preferences.getString(key, default.toString())
        origValue ?: return default
        return Integer.parseInt(origValue)
    }

    /**
     * Get a boolean value from the preferences.
     */
    fun getBoolean(key: String, default: Boolean) = preferences.getBoolean(key, default)

    /**
     * Put a string in the preferences.
     */
    fun putString(key: String, value: String) {
        val edit = preferences.edit()
        edit.putString(key, value)
        edit.apply()
    }

    /**
     * Create or update an int in the preferences.
     */
    fun putInt(key: String, value: Int) {
        val edit = preferences.edit()
        edit.putInt(key, value)
        edit.apply()
    }

    /**
     * Create or update a boolean in the preferences.
     */
    fun putBoolean(key: String, value: Boolean) {
        val edit = preferences.edit()
        edit.putBoolean(key, value)
        edit.apply()
    }

    companion object {
        /**
         * The filename used to store the preferences.
         */
        const val FILE_NAME = "dev.leonlatsch.photok_preferences"

        /**
         * Always use private mode to open preferences.
         */
        const val MODE = Context.MODE_PRIVATE

        /**
         * Determines if the app has started before.
         */
        const val SYSTEM_FIRST_START = "system^firstStart"
        const val SYSTEM_FIRST_START_DEFAULT  = true

        /**
         * Determines if the full screen photo view, should hide the system ui at start.
         */
        const val GALLERY_AUTO_FULLSCREEN = "gallery^fullscreen.auto"
        const val GALLERY_AUTO_FULLSCREEN_DEFAULT = true

        /**
         * Determines the gallery columns.
         */
        const val GALLERY_ADVANCED_GALLERY_COLUMNS = "gallery^advanced.galleryColumns"
        const val GALLERY_ADVANCED_GALLERY_COLUMNS_DEFAULT = 4

        /**
         * Determines if screenshots should be allowed.
         */
        const val SECURITY_ALLOW_SCREENSHOTS = "security^allowScreenshots"
        const val SECURITY_ALLOW_SCREENSHOTS_DEFAULT = false
    }
}