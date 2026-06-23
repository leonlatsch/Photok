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

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.encryption.domain.RecoveryPhraseStore
import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.encryption.domain.VaultService
import dev.leonlatsch.photok.encryption.domain.crypto.Bip39WordCount
import dev.leonlatsch.photok.encryption.domain.models.CreateRequest
import dev.leonlatsch.photok.encryption.domain.models.RecoveryPhrase
import dev.leonlatsch.photok.io.IO
import dev.leonlatsch.photok.uicomponnets.Dialogs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import javax.inject.Inject

data class RecoveryPhraseSetupUiState(
    val phrase: RecoveryPhrase? = null,
    val inputs: Inputs = Inputs(),
) {
    data class Inputs(
        val wordCount: Bip39WordCount = Bip39WordCount.Twelve,
        val loading: Boolean = false,
        val phraseWasSaved: Boolean = false,
        val qrSheetVisible: Boolean = false,
    )
}

sealed interface RecoveryPhraseSetupUiEvent {
    data class UpdateWordCount(val wordCount: Bip39WordCount) : RecoveryPhraseSetupUiEvent
    data class Share(val context: Context, val phrase: RecoveryPhrase) : RecoveryPhraseSetupUiEvent
    data class SaveToFile(val context: Context, val uri: Uri, val phrase: RecoveryPhrase) : RecoveryPhraseSetupUiEvent
    data class CopyToClipboard(val clipboard: Clipboard, val phrase: RecoveryPhrase) : RecoveryPhraseSetupUiEvent
    data object ShowQrCode : RecoveryPhraseSetupUiEvent
    data object DismissQrSheet : RecoveryPhraseSetupUiEvent
    data object MarkPhraseSaved : RecoveryPhraseSetupUiEvent
}

@HiltViewModel
class RecoveryPhraseSetupViewModel @Inject constructor(
    private val io: IO,
    private val sessionRepository: SessionRepository,
    private val recoveryPhraseStore: RecoveryPhraseStore,
    private val vaultService: VaultService,
) : ViewModel() {

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

            is RecoveryPhraseSetupUiEvent.Share -> {
                val text = """
                    Photok Recovery Phrase

                    ${event.phrase.toMnemonicString()}
                """.trimIndent()

                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, text)
                    type = "text/plain"
                }

                event.context.startActivity(Intent.createChooser(sendIntent, null))

                inputs.update {
                    it.copy(phraseWasSaved = true)
                }
            }

            is RecoveryPhraseSetupUiEvent.SaveToFile -> viewModelScope.launch(Dispatchers.IO) {
                val phraseAsBytes = event.phrase.toMnemonicString().toByteArray()
                val inputStream = ByteArrayInputStream(phraseAsBytes)

                val outputStream = io.openFileOutput(event.uri)
                outputStream ?: return@launch

                io.copy(inputStream, outputStream)

                val fileName = io.getFileName(event.uri)
                Dialogs.showLongToast(event.context, "Saved to $fileName")

                inputs.update {
                    it.copy(phraseWasSaved = true)
                }
            }

            is RecoveryPhraseSetupUiEvent.CopyToClipboard -> {
                viewModelScope.launch {
                    val clipData = ClipData.newPlainText("photok-recovery-phrase", event.phrase.toMnemonicString())
                    event.clipboard.setClipEntry(ClipEntry(clipData))

                    inputs.update {
                        it.copy(phraseWasSaved = true)
                    }
                }
            }

            RecoveryPhraseSetupUiEvent.ShowQrCode -> {
                inputs.update { it.copy(qrSheetVisible = true) }
            }

            RecoveryPhraseSetupUiEvent.DismissQrSheet -> {
                inputs.update { it.copy(qrSheetVisible = false) }
            }

            RecoveryPhraseSetupUiEvent.MarkPhraseSaved -> {
                inputs.update { it.copy(phraseWasSaved = true) }
            }
        }
    }
}
