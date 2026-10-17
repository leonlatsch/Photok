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

import android.content.Context
import android.net.Uri
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
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionType
import dev.leonlatsch.photok.other.AndroidFeatures
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecoveryPhraseUiState(
    val phrase: RecoveryPhrase? = null,
    val inputs: Inputs = Inputs(),
) {
    data class Inputs(
        val wordCount: Bip39WordCount = Bip39WordCount.Twelve,
        val loading: Boolean = false,
        val savedTo: RecoveryPhraseSavedTo? = null,
        val regenerated: Boolean = false,
        val showQrCode: Boolean = false,
    )
}

sealed interface RecoveryPhraseUiEvent {
    data class SaveImageToPhotos(val context: Context, val phrase: RecoveryPhrase) : RecoveryPhraseUiEvent
    data class SaveImageFile(val context: Context, val uri: Uri, val phrase: RecoveryPhrase) : RecoveryPhraseUiEvent
    data class ShareImage(val context: Context, val phrase: RecoveryPhrase) : RecoveryPhraseUiEvent
    data class SaveTextFile(val uri: Uri, val phrase: RecoveryPhrase) : RecoveryPhraseUiEvent
    data class CopyToClipboard(val clipboard: Clipboard, val phrase: RecoveryPhrase) : RecoveryPhraseUiEvent
    data class CreateNewPhrase(val wordCount: Bip39WordCount) : RecoveryPhraseUiEvent
    data object ShowQrCodeSide : RecoveryPhraseUiEvent
    data object ShowWordsSide : RecoveryPhraseUiEvent
}

sealed interface RecoveryPhraseNavigationEvent {
    data object PickImageFileLocation : RecoveryPhraseNavigationEvent
}

@HiltViewModel
class RecoveryPhraseViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val recoveryPhraseStore: RecoveryPhraseStore,
    private val vaultService: VaultService,
    private val exporter: RecoveryPhraseExporter,
) : ViewModel() {

    private val inputs = MutableStateFlow(RecoveryPhraseUiState.Inputs())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val phrase = sessionRepository.observe().flatMapLatest { session ->
        // The vault can be locked while this screen is alive (e.g. coming back from background).
        // There is no phrase to decrypt without a session.
        session?.let { recoveryPhraseStore.observe(it) } ?: flowOf(null)
    }

    val uiState = combine(
        phrase,
        inputs,
    ) { phrase, inputs ->
        RecoveryPhraseUiState(
            phrase = phrase,
            inputs = inputs,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), RecoveryPhraseUiState())

    private val navigationChannel = Channel<RecoveryPhraseNavigationEvent>(Channel.UNLIMITED)
    val navigationEvents = navigationChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            exporter.sharedTo.collect { shared ->
                inputs.update { it.copy(savedTo = shared) }
            }
        }

        viewModelScope.launch {
            val session = sessionRepository.get() ?: return@launch

            if (!vaultService.isSetup(VaultProtectionType.RecoveryPhrase)) {
                vaultService.create(
                    CreateRequest.RecoveryPhrase(session, Bip39WordCount.Twelve)
                )
            }
        }
    }

    fun handleUiEvent(event: RecoveryPhraseUiEvent) {
        when (event) {
            is RecoveryPhraseUiEvent.SaveImageToPhotos -> viewModelScope.launch {
                if (!AndroidFeatures.mediaStore()) {
                    navigationChannel.trySend(RecoveryPhraseNavigationEvent.PickImageFileLocation)
                    return@launch
                }

                inputs.update { it.copy(loading = true) }
                val saved = exporter.saveImageToPhotos(event.context, event.phrase)
                inputs.update { it.copy(loading = false) }

                if (saved == null) {
                    navigationChannel.trySend(RecoveryPhraseNavigationEvent.PickImageFileLocation)
                } else {
                    inputs.update { it.copy(savedTo = saved) }
                }
            }

            is RecoveryPhraseUiEvent.SaveImageFile -> viewModelScope.launch {
                inputs.update { it.copy(loading = true) }
                val saved = exporter.saveImageFile(event.context, event.uri, event.phrase)
                inputs.update { it.copy(loading = false) }

                if (saved != null) {
                    inputs.update { it.copy(savedTo = saved) }
                }
            }

            is RecoveryPhraseUiEvent.ShareImage -> viewModelScope.launch {
                inputs.update { it.copy(loading = true) }
                exporter.shareImage(event.context, event.phrase)
                inputs.update { it.copy(loading = false) }
            }

            is RecoveryPhraseUiEvent.SaveTextFile -> viewModelScope.launch {
                val saved = exporter.saveTextFile(event.uri, event.phrase)

                if (saved != null) {
                    inputs.update { it.copy(savedTo = saved) }
                }
            }

            is RecoveryPhraseUiEvent.CopyToClipboard -> viewModelScope.launch {
                val saved = exporter.copyToClipboard(event.clipboard, event.phrase)
                inputs.update { it.copy(savedTo = saved) }
            }

            is RecoveryPhraseUiEvent.CreateNewPhrase -> {
                val session = sessionRepository.get()
                session ?: return // This event cannot be used if not logged in

                recoveryPhraseStore.clear()

                inputs.update {
                    it.copy(
                        wordCount = event.wordCount,
                        loading = true,
                        savedTo = null,
                        regenerated = true,
                    )
                }

                viewModelScope.launch(Dispatchers.IO) {
                    vaultService.create(
                        CreateRequest.RecoveryPhrase(
                            session,
                            event.wordCount,
                        )
                    )

                    inputs.update { it.copy(loading = false) }
                }
            }

            RecoveryPhraseUiEvent.ShowQrCodeSide -> {
                inputs.update { it.copy(showQrCode = true) }
            }

            RecoveryPhraseUiEvent.ShowWordsSide -> {
                inputs.update { it.copy(showQrCode = false) }
            }
        }
    }

    override fun onCleared() {
        exporter.clearShareCache()
    }
}
