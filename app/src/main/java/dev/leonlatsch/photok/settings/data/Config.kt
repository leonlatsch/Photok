/*
 *   Copyright 2020-2021 Leon Latsch
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

package dev.leonlatsch.photok.settings.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages reading and writing with the config file.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class Config(context: Context) {

    private val preferences: SharedPreferences = context.getSharedPreferences(FILE_NAME, MODE)

    /**
     * Determines if the app has started before.
     */
    var systemFirstStart: Boolean
        get() = getBoolean(SYSTEM_FIRST_START, SYSTEM_FIRST_START_DEFAULT)
        set(value) = putBoolean(SYSTEM_FIRST_START, value)

    /**
     * The version code of the last app version.
     * Updates after showing new features.
     */
    var systemLastFeatureVersionCode: Int
        get() = getInt(SYSTEM_LAST_FEATURE_VERSION_CODE, SYSTEM_LAST_FEATURE_VERSION_CODE_DEFAULT)
        set(value) = putInt(SYSTEM_LAST_FEATURE_VERSION_CODE, value)

    /**
     * Sets the app design to "light", "dark" or "system"
     */
    var systemDesign: String?
        get() = getString(SYSTEM_DESIGN, SYSTEM_DESIGN_DEFAULT)
        set(value) = putString(SYSTEM_DESIGN, value!!)

    /**
     * Determines if the full screen photo view, should hide the system ui at start.
     */
    var galleryAutoFullscreen: Boolean
        get() = getBoolean(GALLERY_AUTO_FULLSCREEN, GALLERY_AUTO_FULLSCREEN_DEFAULT)
        set(value) = putBoolean(GALLERY_AUTO_FULLSCREEN, value)

    /**
     * Determines if screenshots should be allowed.
     */
    var securityAllowScreenshots: Boolean
        get() = getBoolean(SECURITY_ALLOW_SCREENSHOTS, SECURITY_ALLOW_SCREENSHOTS_DEFAULT)
        set(value) = putBoolean(SECURITY_ALLOW_SCREENSHOTS, value)

    /**
     * Password hash to check when unlocking.
     */
    var securityPassword: String?
        get() = getString(SECURITY_PASSWORD, SECURITY_PASSWORD_DEFAULT)
        set(value) = putString(SECURITY_PASSWORD, value!!)

    /**
     * Timeout to auto lock when in background.
     */
    var securityLockTimeout: Int
        get() = getIntFromString(SECURITY_LOCK_TIMEOUT, SECURITY_LOCK_TIMEOUT_DEFAULT)
        set(value) = putString(SECURITY_LOCK_TIMEOUT, value.toString())

    /**
     * Launch code to launch from phone dialer.
     */
    var securityDialLaunchCode: String?
        get() = getString(SECURITY_DIAL_LAUNCH_CODE, SECURITY_DIAL_LAUNCH_CODE_DEFAULT)
        set(value) = putString(SECURITY_DIAL_LAUNCH_CODE, value!!)


    // region put/get methods

    private fun getString(key: String, default: String) = preferences.getString(key, default)

    private fun getInt(key: String, default: Int) = preferences.getInt(key, default)

    private fun getIntFromString(key: String, default: Int): Int {
        val stringValue = preferences.getString(key, default.toString())
        return stringValue?.toInt() ?: default
    }

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


        const val SYSTEM_FIRST_START = "system^firstStart"
        const val SYSTEM_FIRST_START_DEFAULT = true

        const val SYSTEM_LAST_FEATURE_VERSION_CODE = "system^lastFeatureVersionCode"
        const val SYSTEM_LAST_FEATURE_VERSION_CODE_DEFAULT = 0

        const val SYSTEM_DESIGN = "system^design"
        const val SYSTEM_DESIGN_DEFAULT = "system"


        const val GALLERY_AUTO_FULLSCREEN = "gallery^fullscreen.auto"
        const val GALLERY_AUTO_FULLSCREEN_DEFAULT = true


        const val SECURITY_ALLOW_SCREENSHOTS = "security^allowScreenshots"
        const val SECURITY_ALLOW_SCREENSHOTS_DEFAULT = false


        const val SECURITY_PASSWORD = "security^password"
        const val SECURITY_PASSWORD_DEFAULT = ""


        const val SECURITY_LOCK_TIMEOUT = "security^lockTimeout"
        const val SECURITY_LOCK_TIMEOUT_DEFAULT = 300000

        const val SECURITY_DIAL_LAUNCH_CODE = "security^dialLaunchCode"
        const val SECURITY_DIAL_LAUNCH_CODE_DEFAULT = "1337"
    }
}