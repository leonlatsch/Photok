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

package dev.leonlatsch.photok.settings.domain

import dev.leonlatsch.photok.R

val ProPreferenceScreenConfig = PreferenceScreenConfig(
    sections = listOf(
        PreferenceSection(
            title = R.string.settings_category_app,
            summary = null,
            preferences = listOf(
                AppPreferences.SystemDesign,
                AppPreferences.GalleryStartPage,
            ),
        ),
        PreferenceSection(
            title = R.string.settings_category_security,
            summary = null,
            preferences = listOf(
                AppPreferences.Screenshots,
                AppPreferences.ChangePassword,
                AppPreferences.Biometric,
                AppPreferences.LockTimeout,
                AppPreferences.LaunchCode,
                AppPreferences.HideApp,
                AppPreferences.RecoveryPhrase,
                ProPreferences.PanicLock,
            ),
        ),
        PreferenceSection(
            title = R.string.settings_category_advanced,
            summary = R.string.settings_category_advanced_summary,
            preferences = listOf(
                AppPreferences.Backup,
                AppPreferences.DeleteImportedFiles,
                AppPreferences.DeleteExportedFiles,
                AppPreferences.Reset,
            ),
        ),
        PreferenceSection(
            title = R.string.settings_other_title,
            summary = null,
            preferences = listOf(
                AppPreferences.Feedback,
                AppPreferences.Donate,
                AppPreferences.SourceCode,
                AppPreferences.Credits,
                AppPreferences.Telemetry,
                AppPreferences.About,
            ),
        ),
    ),
)
