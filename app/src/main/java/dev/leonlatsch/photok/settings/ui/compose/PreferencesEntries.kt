/*
 *   Copyright 2020-2024 Leon Latsch
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

package dev.leonlatsch.photok.settings.ui.compose

import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.settings.data.Config.Companion.GALLERY_AUTO_FULLSCREEN
import dev.leonlatsch.photok.settings.data.Config.Companion.GALLERY_AUTO_FULLSCREEN_DEFAULT
import dev.leonlatsch.photok.settings.data.Config.Companion.GALLERY_START_PAGE
import dev.leonlatsch.photok.settings.data.Config.Companion.GALLERY_START_PAGE_DEFAULT
import dev.leonlatsch.photok.settings.data.Config.Companion.SECURITY_ALLOW_SCREENSHOTS
import dev.leonlatsch.photok.settings.data.Config.Companion.SECURITY_ALLOW_SCREENSHOTS_DEFAULT
import dev.leonlatsch.photok.settings.data.Config.Companion.SECURITY_BIOMETRIC_AUTHENTICATION_ENABLED
import dev.leonlatsch.photok.settings.data.Config.Companion.SECURITY_BIOMETRIC_AUTHENTICATION_ENABLED_DEFAULT
import dev.leonlatsch.photok.settings.data.Config.Companion.SYSTEM_DESIGN
import dev.leonlatsch.photok.settings.data.Config.Companion.SYSTEM_DESIGN_DEFAULT
import dev.leonlatsch.photok.settings.domain.models.SettingsEntry

/**
 * Entries for the preferences screen. Used for visual representation.
 * Use const values from Config for key and default.
 */
object PreferencesEntries {

    val SystemDesign = SettingsEntry(
        key = SYSTEM_DESIGN,
        default = SYSTEM_DESIGN_DEFAULT,
        icon = R.drawable.ic_brush,
        title = R.string.settings_app_design_title,
        summary = null,
    )

    val GalleryAutoFullscreen = SettingsEntry(
        key = GALLERY_AUTO_FULLSCREEN,
        default = GALLERY_AUTO_FULLSCREEN_DEFAULT,
        icon = R.drawable.ic_fullscreen,
        title = R.string.settings_gallery_auto_fullscreen_title,
        summary = R.string.settings_gallery_auto_fullscreen_summary,
    )

    val GalleryStartPage = SettingsEntry(
        key = GALLERY_START_PAGE,
        default = GALLERY_START_PAGE_DEFAULT,
        icon = R.drawable.ic_gallery_thumbnail,
        title = R.string.settings_gallery_start_page_title,
        summary = null,
    )

    val SecurityAllowScreenshots = SettingsEntry(
        key = SECURITY_ALLOW_SCREENSHOTS,
        default = SECURITY_ALLOW_SCREENSHOTS_DEFAULT,
        icon = R.drawable.ic_screen_lock,
        title = R.string.settings_security_allow_screenshots_title,
        summary = R.string.settings_security_allow_screenshots_summary,
    )

    val BiometricUnlock = SettingsEntry(
        key = SECURITY_BIOMETRIC_AUTHENTICATION_ENABLED,
        default = SECURITY_BIOMETRIC_AUTHENTICATION_ENABLED_DEFAULT,
        icon = R.drawable.ic_fingerprint,
        title = R.string.settings_security_biometric_title,
        summary = R.string.settings_security_biometric_summary,
    )
}
