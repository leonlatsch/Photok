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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.leonlatsch.photok.settings.domain.models.SettingsEnum

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
    val proFeature: Boolean

    data class Simple(
        override val key: String,
        override val icon: Int,
        override val title: Int,
        val summary: Int,
        override val proFeature: Boolean = false,
    ) : Preference

    data class Switch(
        override val key: String,
        override val icon: Int,
        override val title: Int,
        val summary: Int,
        val default: Boolean,
        override val proFeature: Boolean = false,
    ) : Preference

    data class Enum<T : SettingsEnum>(
        override val key: String,
        override val icon: Int,
        override val title: Int,
        val explanation: Int? = null,
        val default: T,
        val possibleValues: List<T>,
        override val proFeature: Boolean = false,
    ) : Preference

    data class Page(
        override val key: String,
        override val icon: Int,
        override val title: Int,
        @get:StringRes val summary: Int,
        val subPageConfig: PreferenceScreenConfig,
        override val proFeature: Boolean = false,
    ) : Preference
}
