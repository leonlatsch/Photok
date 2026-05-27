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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ViewRecoveryPhraseUiState(val phrase: RecoveryPhrase = RecoveryPhrase(emptyList()))

@HiltViewModel
class ViewRecoveryPhraseViewModel @Inject constructor(
    recoveryPhraseStore: RecoveryPhraseStore,
    sessionRepository: SessionRepository,
) : ViewModel() {

    val uiState = recoveryPhraseStore.observe(sessionRepository.require()).map {
        ViewRecoveryPhraseUiState(
            phrase = it ?: RecoveryPhrase(emptyList()),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ViewRecoveryPhraseUiState())
}