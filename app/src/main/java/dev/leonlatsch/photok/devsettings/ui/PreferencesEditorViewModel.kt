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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.devsettings.domain.PreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PreferencesEditorViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<PreferencesEditorUiState> = preferencesRepository.observeFiles()
        .map { PreferencesEditorUiState.Content(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), PreferencesEditorUiState.Loading)

    fun handleUiEvent(event: PreferencesEditorUiEvent) {
        when (event) {
            is PreferencesEditorUiEvent.SaveString -> preferencesRepository.putString(event.file, event.key, event.value)
            is PreferencesEditorUiEvent.SaveInt -> preferencesRepository.putInt(event.file, event.key, event.value)
            is PreferencesEditorUiEvent.SaveLong -> preferencesRepository.putLong(event.file, event.key, event.value)
            is PreferencesEditorUiEvent.SaveFloat -> preferencesRepository.putFloat(event.file, event.key, event.value)
            is PreferencesEditorUiEvent.SaveBoolean -> preferencesRepository.putBoolean(event.file, event.key, event.value)
            is PreferencesEditorUiEvent.Remove -> preferencesRepository.remove(event.file, event.key)
        }
    }
}
