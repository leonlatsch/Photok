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

package dev.leonlatsch.photok.recovery.ui

import android.app.Activity
import android.content.res.Resources
import android.net.Uri
import androidx.compose.ui.platform.Clipboard
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.encryption.domain.VaultService
import dev.leonlatsch.photok.encryption.domain.crypto.Bip39MnemonicGenerator
import dev.leonlatsch.photok.encryption.domain.crypto.Bip39WordCount
import dev.leonlatsch.photok.encryption.domain.models.RecoveryPhrase
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import dev.leonlatsch.photok.io.IO
import dev.leonlatsch.photok.review.InAppReview
import dev.leonlatsch.photok.review.ReviewTrigger
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

// 24 words × ~10 chars + separators — anything larger is not a phrase file.
private const val MAX_PHRASE_FILE_BYTES = 512

data class RecoveryPhraseRestoreUiState(
    val words: List<String> = List(Bip39WordCount.Twelve.words) { "" },
    val wordCount: Bip39WordCount = Bip39WordCount.Twelve,
    val unknownWordIndices: Set<Int> = emptySet(),
    val showScanner: Boolean = false,
    val revealToken: Int = 0,
    val source: String? = null,
    val error: String? = null,
    val loading: Boolean = false,
    val unlocked: Boolean = false,
    val phraseValid: Boolean = false,
) {
    data class Inputs(
        val words: List<String> = List(Bip39WordCount.Twelve.words) { "" },
        val wordCount: Bip39WordCount = Bip39WordCount.Twelve,
        val showScanner: Boolean = false,
        val revealToken: Int = 0,
        val source: String? = null,
        val error: String? = null,
        val loading: Boolean = false,
        val unlocked: Boolean = false,
    )
}

sealed interface RecoveryPhraseRestoreUiEvent {
    data class UpdateWord(val index: Int, val word: String) : RecoveryPhraseRestoreUiEvent
    data class FillWords(val startIndex: Int, val words: List<String>) : RecoveryPhraseRestoreUiEvent
    data class SelectWordCount(val wordCount: Bip39WordCount) : RecoveryPhraseRestoreUiEvent
    data object ShowScanner : RecoveryPhraseRestoreUiEvent
    data object ShowWords : RecoveryPhraseRestoreUiEvent
    data class QrScanned(val raw: String) : RecoveryPhraseRestoreUiEvent
    data class PasteFromClipboard(val clipboard: Clipboard) : RecoveryPhraseRestoreUiEvent
    data class LoadFromFile(val uri: Uri) : RecoveryPhraseRestoreUiEvent
    data object Unlock : RecoveryPhraseRestoreUiEvent
}

@HiltViewModel
class RecoveryPhraseRestoreViewModel @Inject constructor(
    private val resources: Resources,
    private val sessionRepository: SessionRepository,
    private val vaultService: VaultService,
    private val mnemonicGenerator: Bip39MnemonicGenerator,
    private val io: IO,
    private val inAppReview: InAppReview,
) : ViewModel() {

    fun requestInAppReview(activity: Activity) {
        inAppReview.requestInAppReview(activity, ReviewTrigger.RecoveryPhraseUsed)
    }

    private val inputs = MutableStateFlow(RecoveryPhraseRestoreUiState.Inputs())

    val uiState = inputs.map { inputs ->
        RecoveryPhraseRestoreUiState(
            words = inputs.words,
            wordCount = inputs.wordCount,
            unknownWordIndices = inputs.words.indices
                .filter { inputs.words[it].isNotEmpty() && !mnemonicGenerator.hasWordStartingWith(inputs.words[it]) }
                .toSet(),
            showScanner = inputs.showScanner,
            revealToken = inputs.revealToken,
            source = inputs.source,
            error = inputs.error,
            loading = inputs.loading,
            unlocked = inputs.unlocked,
            phraseValid = RecoveryPhrase(inputs.words).validate(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), RecoveryPhraseRestoreUiState())

    fun handleUiEvent(event: RecoveryPhraseRestoreUiEvent) {
        if (inputs.value.unlocked) return

        when (event) {
            is RecoveryPhraseRestoreUiEvent.UpdateWord -> inputs.update {
                it.copy(
                    words = it.words.toMutableList().apply { set(event.index, event.word) },
                    source = null,
                    error = null,
                )
            }

            is RecoveryPhraseRestoreUiEvent.FillWords -> inputs.update {
                val wordCount = if (event.startIndex + event.words.size > Bip39WordCount.Twelve.words) {
                    Bip39WordCount.TwentyFour
                } else {
                    it.wordCount
                }
                val words = it.words.resizedTo(wordCount).toMutableList()
                event.words.forEachIndexed { offset, word ->
                    if (event.startIndex + offset < words.size) {
                        words[event.startIndex + offset] = word
                    }
                }

                it.copy(
                    words = words,
                    wordCount = wordCount,
                    revealToken = it.revealToken + 1,
                    source = null,
                    error = null,
                )
            }

            is RecoveryPhraseRestoreUiEvent.SelectWordCount -> inputs.update {
                it.copy(
                    words = it.words.resizedTo(event.wordCount),
                    wordCount = event.wordCount,
                )
            }

            RecoveryPhraseRestoreUiEvent.ShowScanner -> inputs.update {
                it.copy(showScanner = true, error = null)
            }

            RecoveryPhraseRestoreUiEvent.ShowWords -> inputs.update {
                it.copy(showScanner = false)
            }

            is RecoveryPhraseRestoreUiEvent.QrScanned -> {
                val words = event.raw.toRecoveryPhraseWords()
                if (RecoveryPhrase(words).validate()) {
                    showLoadedPhrase(words, resources.getString(R.string.recovery_phrase_restore_source_qr))
                } else {
                    showError(resources.getString(R.string.recovery_phrase_restore_error_invalid_qr))
                }
            }

            is RecoveryPhraseRestoreUiEvent.LoadFromFile -> viewModelScope.launch(IO) {
                io.openFileInput(event.uri)?.use { inputStream ->
                    val filename = io.getFileName(event.uri)

                    // A recovery phrase file is always tiny; cap to prevent OOM from a malicious file.
                    val buffer = ByteArray(MAX_PHRASE_FILE_BYTES)
                    val bytesRead = inputStream.read(buffer).coerceAtLeast(0)

                    val words = String(buffer, 0, bytesRead).toRecoveryPhraseWords()
                    if (RecoveryPhrase(words).validate()) {
                        showLoadedPhrase(words, resources.getString(R.string.recovery_phrase_restore_source_file, filename))
                    } else {
                        showError(resources.getString(R.string.recovery_phrase_restore_error_invalid))
                    }
                }
            }

            is RecoveryPhraseRestoreUiEvent.PasteFromClipboard -> viewModelScope.launch {
                val clipEntry = event.clipboard.getClipEntry() ?: return@launch
                if (clipEntry.clipData.itemCount < 1) {
                    return@launch
                }

                val pastedText = clipEntry.clipData.getItemAt(0).text?.toString().orEmpty()
                val words = pastedText.toRecoveryPhraseWords()
                if (RecoveryPhrase(words).validate()) {
                    showLoadedPhrase(words, resources.getString(R.string.recovery_phrase_restore_source_clipboard))
                } else {
                    showError(resources.getString(R.string.recovery_phrase_restore_error_invalid))
                }
            }

            RecoveryPhraseRestoreUiEvent.Unlock -> {
                val phrase = RecoveryPhrase(inputs.value.words)
                inputs.update {
                    it.copy(
                        showScanner = false,
                        loading = true,
                        error = null,
                    )
                }

                viewModelScope.launch {
                    delay(1.seconds)
                    vaultService.unlock(UnlockRequest.RecoveryPhrase(phrase))
                        .onSuccess { session ->
                            sessionRepository.set(session)
                            inputs.update {
                                it.copy(
                                    unlocked = true,
                                    source = null,
                                )
                            }
                        }
                        .onFailure {
                            inputs.update {
                                it.copy(
                                    loading = false,
                                    error = resources.getString(R.string.recovery_phrase_restore_error_generic),
                                )
                            }
                        }
                }
            }
        }
    }

    private fun showLoadedPhrase(words: List<String>, source: String) {
        inputs.update {
            it.copy(
                words = words,
                wordCount = if (words.size == Bip39WordCount.TwentyFour.words) Bip39WordCount.TwentyFour else Bip39WordCount.Twelve,
                showScanner = false,
                revealToken = it.revealToken + 1,
                source = source,
                error = null,
            )
        }
    }

    private fun showError(error: String) {
        inputs.update {
            it.copy(
                showScanner = false,
                source = null,
                error = error,
            )
        }
    }

    private fun List<String>.resizedTo(wordCount: Bip39WordCount): List<String> =
        List(wordCount.words) { getOrNull(it).orEmpty() }
}
