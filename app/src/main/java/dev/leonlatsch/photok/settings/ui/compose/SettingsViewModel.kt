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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.settings.domain.models.SettingsEnum
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SettingsUiState(
    val preferencesValues: Map<String, *>,
)

sealed interface SettingsUiEvent {
    data class ToggleSwitch(val key: String, val value: Boolean) : SettingsUiEvent
    data class SetEnumValue(val key: String, val value: SettingsEnum) : SettingsUiEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val config: Config,
) : ViewModel() {
    val uiState = config.valuesFlow.map {  values ->
        SettingsUiState(
            preferencesValues = values,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SettingsUiState(config.values))

    fun handleUiEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.ToggleSwitch -> config.putBoolean(event.key, event.value)
            is SettingsUiEvent.SetEnumValue -> config.putString(event.key, event.value.value)
        }
    }
}