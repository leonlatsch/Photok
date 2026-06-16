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

package dev.leonlatsch.photok.setup.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.encryption.domain.RecoveryPhraseStore
import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.encryption.domain.VaultService
import dev.leonlatsch.photok.encryption.domain.crypto.Bip39WordCount
import dev.leonlatsch.photok.encryption.domain.models.CreateRequest
import dev.leonlatsch.photok.encryption.domain.models.RecoveryPhrase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class RecoveryPhraseSetupUiState(
    val phrase: RecoveryPhrase? = null,
    val inputs: Inputs = Inputs(),
) {
    data class Inputs(
        val wordCount: Bip39WordCount = Bip39WordCount.Twelve,
        val loading: Boolean = false,
        val phraseWasSaved: Boolean = true,
    )
}

sealed interface RecoveryPhraseSetupUiEvent {
    data class UpdateWordCount(val wordCount: Bip39WordCount) : RecoveryPhraseSetupUiEvent
}

@HiltViewModel
class RecoveryPhraseSetupViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val recoveryPhraseStore: RecoveryPhraseStore,
    private val vaultService: VaultService,
) : ViewModel() {

    //    private val phrase = MutableStateFlow<RecoveryPhrase?>(null)
    private val inputs = MutableStateFlow(RecoveryPhraseSetupUiState.Inputs())

    val uiState = combine(
        recoveryPhraseStore.observe(sessionRepository.require()),
        inputs,
    ) { phrase, inputs ->
        RecoveryPhraseSetupUiState(
            phrase = phrase,
            inputs = inputs,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), RecoveryPhraseSetupUiState())

    fun handleUiEvent(event: RecoveryPhraseSetupUiEvent) {
        when (event) {
            is RecoveryPhraseSetupUiEvent.UpdateWordCount -> {
                if (inputs.value.wordCount == event.wordCount) return

                recoveryPhraseStore.clear()

                inputs.update {
                    it.copy(
                        wordCount = event.wordCount,
                        loading = true,
                    )
                }

                viewModelScope.launch(Dispatchers.IO) {
                    vaultService.create(
                        CreateRequest.RecoveryPhrase(
                            sessionRepository.require(),
                            event.wordCount,
                        )
                    )

                    inputs.update {
                        it.copy(
                            loading = false,
                        )
                    }
                }
            }
        }
    }
}