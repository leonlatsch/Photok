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
import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.encryption.domain.VaultService
import dev.leonlatsch.photok.encryption.domain.crypto.Bip39WordCount
import dev.leonlatsch.photok.encryption.domain.models.RecoveryPhrase
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecoveryPhraseRestoreUiState(
    val words: List<String> = emptyList(),
    val validInput: Boolean = false,
) {
    data class Inputs(
        val words: List<String> = emptyList(),
        val loading: Boolean = false,
        val error: String? = null,
    )
}

sealed interface RecoveryPhraseRestoreUiEvent {
    data class UpdateWords(val words: List<String>) : RecoveryPhraseRestoreUiEvent
    data class Restore(val words: List<String>) : RecoveryPhraseRestoreUiEvent
}

@HiltViewModel
class RecoveryPhraseRestoreViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val vaultService: VaultService,
) : ViewModel() {

    private val inputs = MutableStateFlow(RecoveryPhraseRestoreUiState.Inputs())

    val uiState = inputs.map { inputs ->
        RecoveryPhraseRestoreUiState(
            words = inputs.words,
            validInput = validateWords(inputs.words),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), RecoveryPhraseRestoreUiState())

    fun handleUiEvent(event: RecoveryPhraseRestoreUiEvent) {
        when (event) {
            is RecoveryPhraseRestoreUiEvent.UpdateWords -> {
                inputs.value = inputs.value.copy(words = event.words)
            }
            is RecoveryPhraseRestoreUiEvent.Restore -> {
                inputs.update {
                    it.copy(
                        loading = true,
                    )
                }

                viewModelScope.launch {
                    val phrase = RecoveryPhrase(event.words)
                    vaultService.unlock(UnlockRequest.RecoveryPhrase(phrase))
                        .onSuccess { session ->
                            sessionRepository.set(session)

                            inputs.update {
                                it.copy(
                                    loading = false,
                                )
                            }

                            // TODO: Close screen
                        }
                        .onFailure {
                            inputs.update {
                                it.copy(
                                    loading = false,
                                    error = it.error ?: "Invalid recovery phrase"
                                )
                            }
                        }
                }
            }
        }
    }

    private fun validateWords(words: List<String>): Boolean {
        val validCount = Bip39WordCount.Twelve.words == words.size || Bip39WordCount.TwentyFour.words == words.size

        return validCount // TODO: Add current input to viewmodel and use it here to check if 11 + current is twelve, etc.
    }
}