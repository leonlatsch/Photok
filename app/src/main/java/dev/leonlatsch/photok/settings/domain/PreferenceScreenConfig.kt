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

package dev.leonlatsch.photok.settings.domain

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.settings.data.Config
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
import dev.leonlatsch.photok.settings.domain.models.LockTimeout
import dev.leonlatsch.photok.settings.domain.models.SettingsEnum
import dev.leonlatsch.photok.settings.domain.models.StartPage
import dev.leonlatsch.photok.settings.domain.models.SystemDesignEnum
import dev.leonlatsch.photok.settings.ui.SettingsFragment

data class PreferenceScreenConfig(
    val sections: List<PreferenceSection>,
)

data class PreferenceSection(
    @get:StringRes val title: Int,
    @get:StringRes val summary: Int?,
    val preferences: List<Preference>,
)

sealed interface Preference {
    val key: String
    @get:DrawableRes val icon: Int
    @get:StringRes val title: Int

    data class Simple(
        override val key: String,
        override val icon: Int,
        override val title: Int,
        val summary: Int,
    ) : Preference

    data class Switch(
        override val key: String,
        override val icon: Int,
        override val title: Int,
        val summary: Int,
        val default: Boolean
    ) : Preference

    data class Enum<T: SettingsEnum>(
        override val key: String,
        override val icon: Int,
        override val title: Int,
        val default: T,
        val possibleValues: List<T>,
    ) : Preference
}

val PreferenceScreenConfigContent = listOf<PreferenceSection>(
    PreferenceSection(
        title = R.string.settings_category_app,
        summary = null,
        preferences = listOf(
            Preference.Enum(
                key = SYSTEM_DESIGN,
                icon = R.drawable.ic_brush,
                title = R.string.settings_app_design_title,
                default = SYSTEM_DESIGN_DEFAULT,
                possibleValues = SystemDesignEnum.entries,
            )
        )
    ),
    PreferenceSection(
        title = R.string.settings_category_gallery,
        summary = null,
        preferences = listOf(
            Preference.Switch(
                key = GALLERY_AUTO_FULLSCREEN,
                icon = R.drawable.ic_fullscreen,
                title = R.string.settings_gallery_auto_fullscreen_title,
                summary = R.string.settings_gallery_auto_fullscreen_summary,
                default = GALLERY_AUTO_FULLSCREEN_DEFAULT,
            ),
            Preference.Enum(
                key = GALLERY_START_PAGE,
                icon = R.drawable.ic_gallery_thumbnail,
                title = R.string.settings_gallery_start_page_title,
                default = GALLERY_START_PAGE_DEFAULT,
                possibleValues = StartPage.entries,
            )
        )
    ),
    PreferenceSection(
        title = R.string.settings_category_security,
        summary = null,
        preferences = listOf(
            Preference.Switch(
                key = SECURITY_ALLOW_SCREENSHOTS,
                icon = R.drawable.ic_screen_lock,
                title = R.string.settings_security_allow_screenshots_title,
                summary = R.string.settings_security_allow_screenshots_summary,
                default = SECURITY_ALLOW_SCREENSHOTS_DEFAULT,
            ),
            Preference.Simple(
                key = SettingsFragment.KEY_ACTION_CHANGE_PASSWORD,
                icon = R.drawable.ic_key,
                title = R.string.change_password_title,
                summary = R.string.settings_security_change_password_summary,
            ),
            Preference.Switch(
                key = SECURITY_BIOMETRIC_AUTHENTICATION_ENABLED,
                icon = R.drawable.ic_fingerprint,
                title = R.string.settings_security_biometric_title,
                summary = R.string.settings_security_biometric_summary,
                default = SECURITY_BIOMETRIC_AUTHENTICATION_ENABLED_DEFAULT,
            ),
            Preference.Enum(
                key = Config.SECURITY_LOCK_TIMEOUT,
                icon = R.drawable.ic_schedule,
                title = R.string.settings_security_timeout_title,
                default = LockTimeout.FiveMinute,
                possibleValues = LockTimeout.entries,
            ),
            Preference.Simple(
                key = Config.SECURITY_DIAL_LAUNCH_CODE,
                icon = R.drawable.ic_dialpad,
                title = R.string.settings_security_launch_code_title,
                summary = R.string.settings_security_launch_code_summary,
            ),

            Preference.Simple(
                key = SettingsFragment.KEY_ACTION_HIDE_APP,
                icon = R.drawable.ic_app_blocking,
                title = R.string.settings_security_hide_app_title,
                summary = R.string.settings_security_hide_app_summary,
            ),
        ),
    ),
    PreferenceSection(
        title = R.string.settings_category_advanced,
        summary = R.string.settings_category_advanced_summary,
        preferences = listOf(
            Preference.Simple(
                key = SettingsFragment.KEY_ACTION_RESET,
                icon = R.drawable.ic_warning,
                title = R.string.settings_advanced_reset_title,
                summary = R.string.settings_advanced_reset_summary,
            ),
            Preference.Simple(
                SettingsFragment.KEY_ACTION_BACKUP,
                icon = R.drawable.ic_save_as,
                title = R.string.settings_advanced_backup_title,
                summary = R.string.settings_advanced_backup_summary,
            ),
            Preference.Switch(
                Config.ADVANCED_DELETE_IMPORTED_FILES,
                icon = R.drawable.ic_delete,
                title = R.string.settings_advanced_delete_imported_title,
                summary = R.string.settings_advanced_delete_imported_summary,
                default = Config.ADVANCED_DELETE_IMPORTED_FILES_DEFAULT,
            ),
            Preference.Switch(
                Config.ADVANCED_DELETE_EXPORTED_FILES,
                icon = R.drawable.ic_delete,
                title = R.string.settings_advanced_delete_exported_title,
                summary = R.string.settings_advanced_delete_exported_summary,
                default = Config.ADVANCED_DELETE_EXPORTED_FILES_DEFAULT,
            ),
        ),
    ),
    PreferenceSection(
        title = R.string.settings_other_title,
        summary = null,
        preferences = listOf(
            Preference.Simple(
                key = SettingsFragment.KEY_ACTION_FEEDBACK,
                icon = R.drawable.ic_feedback,
                title = R.string.settings_other_feedback_title,
                summary = R.string.settings_other_feedback_summary,
            ),
            Preference.Simple(
                key = SettingsFragment.KEY_ACTION_DONATE,
                icon = R.drawable.ic_money,
                title = R.string.settings_other_donate_title,
                summary = R.string.settings_other_donate_summary,
            ),
            Preference.Simple(
                key = SettingsFragment.KEY_ACTION_SOURCECODE,
                icon = R.drawable.ic_code,
                title = R.string.settings_other_sourcecode_title,
                summary = R.string.settings_other_sourcecode_summary,
            ),
            Preference.Simple(
                key = SettingsFragment.KEY_ACTION_CREDITS,
                icon = R.drawable.ic_book,
                title = R.string.settings_other_credits_title,
                summary = R.string.settings_other_credits_summary,
            ),
            Preference.Simple(
                key = SettingsFragment.KEY_ACTION_ABOUT,
                icon = R.drawable.ic_info,
                title = R.string.settings_other_about_title,
                summary = R.string.settings_other_about_summary,
            ),
        ),
    )
)

