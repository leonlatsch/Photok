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

package dev.leonlatsch.photok.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.encryption.domain.VaultService
import dev.leonlatsch.photok.encryption.domain.crypto.Bip39WordCount
import dev.leonlatsch.photok.encryption.domain.models.CreateRequest
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import dev.leonlatsch.photok.pro.navigation.ProRoute
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.telemetry.domain.Signal
import dev.leonlatsch.photok.telemetry.domain.TelemetryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val config: Config,
    private val vaultService: VaultService,
    private val sessionRepository: SessionRepository,
    private val telemetryService: TelemetryService,
) : ViewModel() {

    val initialBackStack: List<NavKey> = when (config.systemOnboardingStep) {
        OnboardingRoute.SetupPassword.stepKey() -> listOf(
            OnboardingRoute.SetupPassword,
        )

        OnboardingRoute.BiometricUnlock.stepKey() -> listOf(
            OnboardingRoute.BiometricUnlock,
        )

        OnboardingRoute.RecoveryPhraseSetup.stepKey() -> listOf(
            OnboardingRoute.RecoveryPhraseSetup,
        )

        OnboardingRoute.AddFirstPhotos.stepKey() -> listOf(
            OnboardingRoute.AddFirstPhotos,
        )

        ProRoute.OnboardingOffer.stepKey() -> listOf(
            ProRoute.OnboardingOffer,
        )

        OnboardingRoute.Done.stepKey() -> listOf(
            OnboardingRoute.Done,
        )

        else -> listOf(OnboardingRoute.Welcome)
    }

    val preferencesValues = config.valuesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), config.values)

    private val navigationEventsChannel = Channel<OnboardingNavigationEvent>(Channel.UNLIMITED)
    val navigationEvents = navigationEventsChannel.receiveAsFlow()

    fun handleUiEvent(event: OnboardingUiEvent) {
        when (event) {
            is OnboardingUiEvent.StepChanged -> {
                config.systemOnboardingStep = event.route.stepKey()
            }

            OnboardingUiEvent.GetStartedClicked -> {
                telemetryService.signal(Signal.OnboardingWelcomeGetStartedClicked)
            }

            OnboardingUiEvent.BiometricEnableButtonClicked -> {
                telemetryService.signal(Signal.OnboardingBiometricEnableClicked, mapOf("source" to "button"))
            }

            OnboardingUiEvent.BiometricGlyphClicked -> {
                telemetryService.signal(Signal.OnboardingBiometricEnableClicked, mapOf("source" to "glyph"))
            }

            is OnboardingUiEvent.SetupTestVault -> setupTestVault()

            OnboardingUiEvent.Finish -> {
                telemetryService.signal(Signal.OnboardingFinished)
                finish()
            }
        }
    }

    private fun setupTestVault() {
        if (!BuildConfig.DEBUG) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            vaultService.create(CreateRequest.Password.NewVault("abc123"))

            val result = vaultService.unlock(UnlockRequest.Password("abc123"))
            val session = result.getOrElse { return@launch }
            sessionRepository.set(session)
            vaultService.create(CreateRequest.RecoveryPhrase(session, Bip39WordCount.Twelve))

            finish()
        }
    }

    private fun finish() {
        config.systemFirstStart = false
        config.systemOnboardingStep = null
        navigationEventsChannel.trySend(OnboardingNavigationEvent.Finished)
    }
}
