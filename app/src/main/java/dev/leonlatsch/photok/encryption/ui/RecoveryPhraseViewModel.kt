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

package dev.leonlatsch.photok.encryption.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.encryption.domain.RecoveryPhraseStore
import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.encryption.domain.models.RecoveryPhrase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecoveryPhraseViewModel @Inject constructor(
    private val recoveryPhraseStore: RecoveryPhraseStore,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Loaded(val phrase: RecoveryPhrase) : UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun load() = viewModelScope.launch {
        _uiState.value = UiState.Loading
        val session = runCatching { sessionRepository.require() }.getOrNull() ?: return@launch
        val phrase = recoveryPhraseStore.load(session) ?: return@launch
        _uiState.value = UiState.Loaded(phrase)
    }
}
