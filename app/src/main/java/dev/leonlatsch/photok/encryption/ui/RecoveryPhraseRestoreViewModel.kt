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

import android.net.Uri
import androidx.compose.ui.platform.Clipboard
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.encryption.domain.VaultService
import dev.leonlatsch.photok.encryption.domain.models.RecoveryPhrase
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import dev.leonlatsch.photok.io.IO
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecoveryPhraseRestoreUiState(
    val phrase: RecoveryPhrase = RecoveryPhrase(emptyList()),
    val loading: Boolean = false,
    val error: String? = null,
    val unlocked: Boolean = false,
    val selectedRestoreMethod: RestoreMethod? = null,
    val restoreSupportingText: String? = null,

    val phraseValid: Boolean = false,
) {
    enum class RestoreMethod {
        TypeByHand,
        PasteFromClipboard,
        ScanQrCode,
        LoadFromFile,
    }

    data class Inputs(
        val phrase: RecoveryPhrase = RecoveryPhrase(emptyList()),
        val loading: Boolean = false,
        val error: String? = null,
        val selectedRestoreMethod: RestoreMethod? = null,
        val restoreSupportingText: String? = null,
        val unlocked: Boolean = false,
    )
}

sealed interface RecoveryPhraseRestoreUiEvent {
    data class Restore(val phrase: RecoveryPhrase) : RecoveryPhraseRestoreUiEvent
    data class UpdatePhrase(val phrase: RecoveryPhrase) : RecoveryPhraseRestoreUiEvent

    data object TypeByHand : RecoveryPhraseRestoreUiEvent
    data object ScanQrCode : RecoveryPhraseRestoreUiEvent
    data class QrScanned(val raw: String) : RecoveryPhraseRestoreUiEvent
    data class PasteFromClipboard(val clipboard: Clipboard) : RecoveryPhraseRestoreUiEvent
    data class LoadFromFile(val uri: Uri) : RecoveryPhraseRestoreUiEvent

    data object ClearRestoreMethod : RecoveryPhraseRestoreUiEvent
}

@HiltViewModel
class RecoveryPhraseRestoreViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val vaultService: VaultService,
    private val io: IO,
) : ViewModel() {

    private val inputs = MutableStateFlow(RecoveryPhraseRestoreUiState.Inputs())

    val uiState = inputs.map { inputs ->
        RecoveryPhraseRestoreUiState(
            phrase = inputs.phrase,
            loading = inputs.loading,
            error = inputs.error,
            unlocked = inputs.unlocked,
            selectedRestoreMethod = inputs.selectedRestoreMethod,
            restoreSupportingText = inputs.restoreSupportingText,

            phraseValid = inputs.phrase.validate(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), RecoveryPhraseRestoreUiState())

    fun handleUiEvent(event: RecoveryPhraseRestoreUiEvent) {
        when (event) {
            is RecoveryPhraseRestoreUiEvent.Restore -> {
                inputs.update {
                    it.copy(
                        loading = true,
                        error = null,
                    )
                }

                viewModelScope.launch {
                    delay(500)
                    vaultService.unlock(UnlockRequest.RecoveryPhrase(event.phrase))
                        .onSuccess { session ->
                            sessionRepository.set(session)

                            inputs.update {
                                it.copy(
                                    loading = false,
                                    unlocked = true,
                                    restoreSupportingText = null,
                                )
                            }
                        }
                        .onFailure {
                            inputs.update {
                                it.copy(
                                    loading = false,
                                    error = "Could not restore vault from recovery phrase.",
                                )
                            }
                        }
                }
            }

            is RecoveryPhraseRestoreUiEvent.TypeByHand -> {
                inputs.update {
                    it.copy(
                        selectedRestoreMethod = if (it.selectedRestoreMethod == null) {
                            RecoveryPhraseRestoreUiState.RestoreMethod.TypeByHand
                        } else {
                            null
                        },
                        phrase = RecoveryPhrase(),
                        error = null,
                        restoreSupportingText = null,
                    )
                }
            }

            is RecoveryPhraseRestoreUiEvent.ScanQrCode -> {
                inputs.update {
                    it.copy(
                        selectedRestoreMethod = if (it.selectedRestoreMethod == null) {
                            RecoveryPhraseRestoreUiState.RestoreMethod.ScanQrCode
                        } else {
                            null
                        },
                        phrase = RecoveryPhrase(),
                        error = null,
                        restoreSupportingText = null,
                    )
                }
            }

            is RecoveryPhraseRestoreUiEvent.QrScanned -> {
                val phrase = RecoveryPhrase.from(event.raw)
                if (phrase.validate()) {
                    inputs.update {
                        it.copy(
                            phrase = phrase,
                            error = null,
                            restoreSupportingText = "Scanned from QR code",
                        )
                    }
                } else {
                    inputs.update {
                        it.copy(
                            error = "Invalid recovery phrase QR code",
                            restoreSupportingText = null,
                        )
                    }
                }
            }

            is RecoveryPhraseRestoreUiEvent.LoadFromFile -> viewModelScope.launch(IO) {
                io.openFileInput(event.uri)?.use { inputStream ->
                    val filename = io.getFileName(event.uri)

                    val bytes = inputStream.readBytes()

                    val phrase = RecoveryPhrase.from(String(bytes))

                    if (phrase.validate()) {
                        inputs.update {
                            it.copy(
                                phrase = phrase,
                                selectedRestoreMethod = RecoveryPhraseRestoreUiState.RestoreMethod.LoadFromFile,
                                error = null,
                                restoreSupportingText = "From $filename",
                            )
                        }
                    } else {
                        inputs.update {
                            it.copy(
                                error = "Invalid recovery phrase",
                                restoreSupportingText = null,
                            )
                        }
                    }
                }
            }

            is RecoveryPhraseRestoreUiEvent.PasteFromClipboard -> viewModelScope.launch {
                val clipEntry = event.clipboard.getClipEntry()
                clipEntry ?: return@launch
                if (clipEntry.clipData.itemCount < 1) {
                    return@launch
                }

                val item = clipEntry.clipData.getItemAt(0)
                val phrase = RecoveryPhrase.from(item.text.toString())

                if (phrase.validate()) {
                    inputs.update {
                        it.copy(
                            phrase = phrase,
                            selectedRestoreMethod = RecoveryPhraseRestoreUiState.RestoreMethod.PasteFromClipboard,
                            error = null,
                            restoreSupportingText = "Pasted from clipboard",
                        )
                    }
                } else {
                    inputs.update {
                        it.copy(
                            error = "Invalid recovery phrase",
                            restoreSupportingText = null,
                        )
                    }
                }
            }

            is RecoveryPhraseRestoreUiEvent.UpdatePhrase -> inputs.update {
                it.copy(phrase = event.phrase)
            }

            RecoveryPhraseRestoreUiEvent.ClearRestoreMethod -> inputs.update {
                it.copy(selectedRestoreMethod = null, phrase = RecoveryPhrase(), restoreSupportingText = null)
            }
        }
    }
}