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

package dev.leonlatsch.photok.main.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute : NavKey {

    @Serializable
    data object RecoveryPhraseSetupFromSettings : AppRoute

    @Serializable
    data object Gallery : AppRoute

    @Serializable
    data object Albums : AppRoute

    @Serializable
    data class AlbumDetail(val albumUuid: String) : AppRoute

    @Serializable
    data class ImageViewer(val photoUuid: String, val albumUuid: String?) : AppRoute

    @Serializable
    data object Settings : AppRoute

    @Serializable
    data object About : AppRoute

    @Serializable
    data object Credits : AppRoute

    @Serializable
    data object OssLicenses : AppRoute

    @Serializable
    data object DevSettings : AppRoute

    @Serializable
    data object PreferencesEditor : AppRoute

    @Serializable
    data class RestoreBackup(val backupUri: String) : AppRoute
}
