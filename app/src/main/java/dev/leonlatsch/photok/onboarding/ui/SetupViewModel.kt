/*
 *   Copyright 2020–2026 Leon Latsch
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

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.encryption.domain.PasswordUtils
import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.encryption.domain.VaultService
import dev.leonlatsch.photok.encryption.domain.crypto.Bip39WordCount
import dev.leonlatsch.photok.encryption.domain.models.CreateRequest
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.telemetry.domain.Signal
import dev.leonlatsch.photok.telemetry.domain.TelemetryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for the setup.
 * Handles password validation, creating the vault and initializing the session.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class SetupViewModel @Inject constructor(
    private val config: Config,
    private val vaultService: VaultService,
    private val sessionRepository: SessionRepository,
    private val telemetryService: TelemetryService,
) : ViewModel() {

    private val inputs = MutableStateFlow(
        SetupUiState.Inputs()
    )

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private val passwordStrength = inputs
        .map { it.password }
        .distinctUntilChanged()
        .mapLatest { password ->
            password
                .takeIf { PasswordUtils.validatePassword(it) }
                ?.let {
                    val locale = AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault()
                    PasswordUtils.estimateStrength(it, locale)
                }
        }
        .flowOn(Dispatchers.Default)

    val uiState = combine(inputs, passwordStrength) { inputs, passwordStrength ->
        SetupUiState(
            password = inputs.password,
            confirmPassword = inputs.confirmPassword,
            loading = inputs.loading,
            passwordStrength = passwordStrength,
            showConfirmPassword = PasswordUtils.validatePassword(inputs.password),
            passwordsMismatch = inputs.confirmPassword.isNotEmpty()
                    && inputs.password != inputs.confirmPassword,
            canSetup = PasswordUtils.validatePasswords(inputs.password, inputs.confirmPassword)
                    && !inputs.loading,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SetupUiState())

    private val navigationEventsChannel = Channel<SetupNavigationEvent>(Channel.UNLIMITED)
    val navigationEvents = navigationEventsChannel.receiveAsFlow()

    fun handleUiEvent(event: SetupUiEvent) {
        if (inputs.value.loading) return

        when (event) {
            is SetupUiEvent.PasswordChanged -> inputs.update {
                // The confirm field is hidden again once the password is no longer valid.
                val confirmPassword = if (PasswordUtils.validatePassword(event.password)) {
                    it.confirmPassword
                } else {
                    ""
                }

                it.copy(password = event.password, confirmPassword = confirmPassword)
            }

            is SetupUiEvent.ConfirmPasswordChanged -> inputs.update {
                it.copy(confirmPassword = event.confirmPassword)
            }

            SetupUiEvent.Setup -> setup()
        }
    }

    private fun setup() {
        val password = inputs.value.password
        if (!PasswordUtils.validatePasswords(password, inputs.value.confirmPassword)) return

        inputs.update { it.copy(loading = true) }

        telemetryService.signal(
            Signal.OnboardingPasswordContinueClicked,
            mapOf("strengthScore" to uiState.value.passwordStrength?.score.toString()),
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                vaultService.create(CreateRequest.Password(password))
                vaultService.unlock(UnlockRequest.Password(password))
                    .onSuccess { session ->
                        sessionRepository.set(session)
                        vaultService.create(
                            CreateRequest.RecoveryPhrase(
                                session,
                                Bip39WordCount.Twelve
                            )
                        )

                        config.systemOnboardingStep = OnboardingRoute.RecoveryPhraseSetup.stepKey()
                        config.justFinishedSetup = true

                        inputs.update { it.copy(loading = false) }
                        navigationEventsChannel.trySend(SetupNavigationEvent.ShowRecoveryPhraseSetup)
                    }
                    .onFailure {
                        Timber.e(it)
                        inputs.update { it.copy(loading = false) }
                        navigationEventsChannel.trySend(SetupNavigationEvent.ShowError)
                    }
            } catch (e: Exception) {
                Timber.e(e)
                inputs.update { it.copy(loading = false) }
                navigationEventsChannel.trySend(SetupNavigationEvent.ShowError)
            }
        }
    }
}
