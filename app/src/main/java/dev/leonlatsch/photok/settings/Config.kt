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

    var systemFirstStart: Boolean
        get() = getBoolean(SYSTEM_FIRST_START, SYSTEM_FIRST_START_DEFAULT)
        set(value) = putBoolean(SYSTEM_FIRST_START, value)

    var galleryAutoFullscreen: Boolean
        get() = getBoolean(GALLERY_AUTO_FULLSCREEN, GALLERY_AUTO_FULLSCREEN_DEFAULT)
        set(value) = putBoolean(GALLERY_AUTO_FULLSCREEN, value)

    var securityAllowScreenshots: Boolean
        get() = getBoolean(SECURITY_ALLOW_SCREENSHOTS, SECURITY_ALLOW_SCREENSHOTS_DEFAULT)
        set(value) = putBoolean(SECURITY_ALLOW_SCREENSHOTS, value)

    var securityPassword: String?
        get() = getString(SECURITY_PASSWORD, SECURITY_PASSWORD_DEFAULT)
        set(value) = putString(SECURITY_PASSWORD, value!!)


    // region put/get methods

    private fun getString(key: String, default: String) = preferences.getString(key, default)

    private fun getInt(key: String, default: Int) = preferences.getInt(key, default)

    private fun getBoolean(key: String, default: Boolean) = preferences.getBoolean(key, default)

    private fun putString(key: String, value: String) {
        val edit = preferences.edit()
        edit.putString(key, value)
        edit.apply()
    }

    private fun putInt(key: String, value: Int) {
        val edit = preferences.edit()
        edit.putInt(key, value)
        edit.apply()
    }

    private fun putBoolean(key: String, value: Boolean) {
        val edit = preferences.edit()
        edit.putBoolean(key, value)
        edit.apply()
    }

    // endregion

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
         * Determines if screenshots should be allowed.
         */
        const val SECURITY_ALLOW_SCREENSHOTS = "security^allowScreenshots"
        const val SECURITY_ALLOW_SCREENSHOTS_DEFAULT = false

        /**
         * Password hash to check when unlockung.
         */
        const val SECURITY_PASSWORD = "security^password"
        const val SECURITY_PASSWORD_DEFAULT = ""
    }
}