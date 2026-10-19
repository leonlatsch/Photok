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

package dev.leonlatsch.photok.devsettings.ui

sealed interface PreferencesEditorUiEvent {
    data class SaveString(val file: String, val key: String, val value: String) : PreferencesEditorUiEvent

    data class SaveInt(val file: String, val key: String, val value: Int) : PreferencesEditorUiEvent

    data class SaveLong(val file: String, val key: String, val value: Long) : PreferencesEditorUiEvent

    data class SaveFloat(val file: String, val key: String, val value: Float) : PreferencesEditorUiEvent

    data class SaveBoolean(val file: String, val key: String, val value: Boolean) : PreferencesEditorUiEvent

    data class Remove(val file: String, val key: String) : PreferencesEditorUiEvent
}
