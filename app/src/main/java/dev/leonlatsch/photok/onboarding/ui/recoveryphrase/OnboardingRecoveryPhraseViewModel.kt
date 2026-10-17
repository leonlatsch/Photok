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

package dev.leonlatsch.photok.onboarding.ui.recoveryphrase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.encryption.domain.RecoveryPhraseStore
import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.encryption.domain.VaultService
import dev.leonlatsch.photok.encryption.domain.crypto.Bip39WordCount
import dev.leonlatsch.photok.encryption.domain.models.CreateRequest
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionType
import dev.leonlatsch.photok.other.AndroidFeatures
import dev.leonlatsch.photok.recovery.ui.RecoveryPhraseExporter
import dev.leonlatsch.photok.recovery.ui.RecoveryPhraseSavedTo
import dev.leonlatsch.photok.settings.domain.ScreenshotOverride
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingRecoveryPhraseViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val recoveryPhraseStore: RecoveryPhraseStore,
    private val vaultService: VaultService,
    private val screenshotOverride: ScreenshotOverride,
    private val exporter: RecoveryPhraseExporter,
) : ViewModel() {

    private val wordCount = MutableStateFlow(Bip39WordCount.Twelve)
    private val loading = MutableStateFlow(false)
    private val savedTo = MutableStateFlow<RecoveryPhraseSavedTo?>(null)
    private val showQrCode = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val phrase = sessionRepository.observe().flatMapLatest { session ->
        session?.let { recoveryPhraseStore.observe(it) } ?: flowOf(null)
    }

    val uiState = combine(
        phrase,
        wordCount,
        loading,
        savedTo,
        showQrCode,
    ) { phrase, wordCount, loading, savedTo, showQrCode ->
        OnboardingRecoveryPhraseUiState(
            phrase = phrase,
            wordCount = wordCount,
            loading = loading,
            savedTo = savedTo,
            showQrCode = showQrCode,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), OnboardingRecoveryPhraseUiState())

    private val navigationChannel = Channel<OnboardingRecoveryPhraseNavigationEvent>(Channel.UNLIMITED)
    val navigationEvents = navigationChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            exporter.sharedTo.collect { savedTo.value = it }
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

    fun handleUiEvent(event: OnboardingRecoveryPhraseUiEvent) {
        when (event) {
            is OnboardingRecoveryPhraseUiEvent.SaveImageToPhotos -> viewModelScope.launch {
                if (!AndroidFeatures.mediaStore()) {
                    navigationChannel.trySend(OnboardingRecoveryPhraseNavigationEvent.PickImageFileLocation)
                    return@launch
                }

                loading.value = true
                val saved = exporter.saveImageToPhotos(event.context, event.phrase)
                loading.value = false

                if (saved == null) {
                    navigationChannel.trySend(OnboardingRecoveryPhraseNavigationEvent.PickImageFileLocation)
                } else {
                    savedTo.value = saved
                }
            }

            is OnboardingRecoveryPhraseUiEvent.SaveImageFile -> viewModelScope.launch {
                loading.value = true
                val saved = exporter.saveImageFile(event.context, event.uri, event.phrase)
                loading.value = false

                if (saved != null) {
                    savedTo.value = saved
                }
            }

            is OnboardingRecoveryPhraseUiEvent.ShareImage -> viewModelScope.launch {
                loading.value = true
                exporter.shareImage(event.context, event.phrase)
                loading.value = false
            }

            is OnboardingRecoveryPhraseUiEvent.SaveTextFile -> viewModelScope.launch {
                val saved = exporter.saveTextFile(event.uri, event.phrase)

                if (saved != null) {
                    savedTo.value = saved
                }
            }

            is OnboardingRecoveryPhraseUiEvent.CopyToClipboard -> viewModelScope.launch {
                savedTo.value = exporter.copyToClipboard(event.clipboard, event.phrase)
            }

            is OnboardingRecoveryPhraseUiEvent.UpdateWordCount -> {
                if (wordCount.value == event.wordCount) return

                val session = sessionRepository.get() ?: return

                recoveryPhraseStore.clear()
                wordCount.value = event.wordCount
                loading.value = true
                savedTo.value = null

                viewModelScope.launch(Dispatchers.IO) {
                    vaultService.create(CreateRequest.RecoveryPhrase(session, event.wordCount))
                    loading.value = false
                }
            }

            OnboardingRecoveryPhraseUiEvent.AllowScreenshots -> {
                screenshotOverride.allow()
            }

            OnboardingRecoveryPhraseUiEvent.RevokeScreenshots -> {
                screenshotOverride.revoke()
            }

            OnboardingRecoveryPhraseUiEvent.ScreenshotTaken -> {
                if (showQrCode.value) {
                    savedTo.value = RecoveryPhraseSavedTo.Screenshot
                }
            }

            OnboardingRecoveryPhraseUiEvent.ShowQrCodeSide -> {
                showQrCode.value = true
            }

            OnboardingRecoveryPhraseUiEvent.ShowWordsSide -> {
                showQrCode.value = false
            }

            OnboardingRecoveryPhraseUiEvent.Continue -> {
                navigationChannel.trySend(OnboardingRecoveryPhraseNavigationEvent.Continue)
            }
        }
    }

    override fun onCleared() {
        screenshotOverride.revoke()
        exporter.clearShareCache()
    }
}
