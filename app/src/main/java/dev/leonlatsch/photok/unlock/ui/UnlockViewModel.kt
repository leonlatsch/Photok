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

package dev.leonlatsch.photok.unlock.ui

import android.content.res.Resources
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.EraseVaultDataUseCase
import dev.leonlatsch.photok.encryption.domain.LegacyEncryption
import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.encryption.domain.VaultService
import dev.leonlatsch.photok.encryption.domain.crypto.Bip39WordCount
import dev.leonlatsch.photok.encryption.domain.models.CreateRequest
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionType
import dev.leonlatsch.photok.encryption.migration.LegacyEncryptionMigrator
import dev.leonlatsch.photok.encryption.ui.BiometricAuthenticationFailedException
import dev.leonlatsch.photok.encryption.ui.UserCanceledBiometricsException
import dev.leonlatsch.photok.pro.domain.PasswordAttemptsResult
import dev.leonlatsch.photok.pro.domain.PasswordAttemptsUseCase
import dev.leonlatsch.photok.pro.intruderwarnings.domain.IntruderWarningCaptureService
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.uicomponnets.Dialogs
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/**
 * ViewModel for unlocking the safe.
 * Handles state, password validation and initializing the encryption.
 * Just like the setup.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class UnlockViewModel @Inject constructor(
    private val config: Config,
    private val resources: Resources,
    private val vaultService: VaultService,
    private val sessionRepository: SessionRepository,
    private val legacyEncryptionMigrator: LegacyEncryptionMigrator,
    private val legacyEncryption: LegacyEncryption,
    private val passwordAttemptsUseCase: PasswordAttemptsUseCase,
    private val intruderWarningCaptureService: IntruderWarningCaptureService,
    private val eraseVaultDataUseCase: EraseVaultDataUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        UnlockUiState(password = if (BuildConfig.DEBUG) "" else "")
    )
    val uiState = _uiState.asStateFlow()

    private val navigationEventsChannel = Channel<UnlockNavigationEvent>()
    val navigationEvents = navigationEventsChannel.receiveAsFlow()

    private var lockoutJob: Job? = null

    init {
        val lockedUntil = passwordAttemptsUseCase.currentLockout()
        if (lockedUntil > System.currentTimeMillis()) {
            lockUntil(lockedUntil)
        }

        viewModelScope.launch {
            val available = vaultService.isSetup(VaultProtectionType.Biometric) ||
                    vaultService.canMigrate(VaultProtectionType.Biometric)

            _uiState.update { it.copy(biometricAvailable = available) }
        }

        viewModelScope.launch {
            val available = vaultService.isSetup(VaultProtectionType.RecoveryPhrase)

            _uiState.update { it.copy(recoveryPhraseAvailable = available) }
        }
    }

    fun handleUiEvent(event: UnlockUiEvent) {
        when (event) {
            is UnlockUiEvent.PasswordChanged -> _uiState.update {
                it.copy(password = event.password, wrongPassword = false)
            }

            UnlockUiEvent.UnlockWithPassword -> unlockWithPassword()

            is UnlockUiEvent.UnlockWithBiometric -> unlockWithBiometric(event.fragment)

            UnlockUiEvent.ForgotPassword -> {
                navigationEventsChannel.trySend(UnlockNavigationEvent.ShowRecoveryPhraseRestore)
            }
        }
    }

    /**
     * Enters the lockout state and leaves it again once [lockedUntil] has passed.
     */
    private fun lockUntil(lockedUntil: Long) {
        _uiState.update { it.copy(lockedUntil = lockedUntil, loading = false) }

        lockoutJob?.cancel()
        lockoutJob = viewModelScope.launch {
            while (System.currentTimeMillis() < lockedUntil) {
                delay(1.seconds)
            }
            dismissLockout()
        }
    }

    private fun dismissLockout() {
        passwordAttemptsUseCase.onSuccessfulUnlock()
        _uiState.update { it.copy(lockedUntil = null) }
    }

    /**
     * Tries to unlock the safe.
     * Compares the entered password to the saved hash.
     */
    private fun unlockWithPassword() {
        val password = _uiState.value.password
        _uiState.update { it.copy(loading = true, wrongPassword = false) }

        viewModelScope.launch {
            try {
                vaultService.unlock(UnlockRequest.Password(password))
                    .onSuccess { session ->
                        passwordAttemptsUseCase.onSuccessfulUnlock()
                        sessionRepository.set(session)

                        if (legacyEncryptionMigrator.migrationNeeded() || config.legacyCurrentlyMigrating) {
                            val legacySession = legacyEncryption.obtainSession(password)
                            legacyEncryptionMigrator.initialize(legacySession)

                            _uiState.update { it.copy(loading = false) }
                            navigationEventsChannel.trySend(UnlockNavigationEvent.StartLegacyMigration)
                        } else if (!vaultService.isSetup(VaultProtectionType.RecoveryPhrase)) {
                            vaultService.create(CreateRequest.RecoveryPhrase(session, Bip39WordCount.Twelve))

                            _uiState.update { it.copy(loading = false) }
                            navigationEventsChannel.trySend(UnlockNavigationEvent.ShowRecoveryPhraseSetup)
                        } else {
                            _uiState.update { it.copy(loading = false) }
                            navigationEventsChannel.trySend(UnlockNavigationEvent.Unlocked)
                        }
                    }
                    .onFailure {
                        viewModelScope.launch {
                            intruderWarningCaptureService.captureWrongPasswordAttempt()
                                .onFailure { error ->
                                    Timber.e(error, "Failed to capture intruder warning")
                                }
                        }

                        when (val result = passwordAttemptsUseCase.onFailedAttempt()) {
                            is PasswordAttemptsResult.Locked -> lockUntil(result.lockedUntil)
                            PasswordAttemptsResult.None -> _uiState.update {
                                it.copy(loading = false, wrongPassword = true)
                            }

                            PasswordAttemptsResult.Erased -> {
                                viewModelScope.launch {
                                    eraseVaultDataUseCase()
                                }
                                _uiState.update { it.copy(loading = false, wrongPassword = true) }
                            }
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.update { it.copy(loading = false) }
                navigationEventsChannel.trySend(UnlockNavigationEvent.ShowError)
            }
        }
    }

    private fun unlockWithBiometric(fragment: Fragment) {
        // A bruteforce lockout must not be skippable by falling back to biometrics.
        if (_uiState.value.lockedUntil != null) return

        viewModelScope.launch {
            vaultService.unlock(UnlockRequest.Biometric(fragment))
                .onSuccess { session ->
                    passwordAttemptsUseCase.onSuccessfulUnlock()
                    sessionRepository.set(session)
                    navigationEventsChannel.trySend(UnlockNavigationEvent.Unlocked)
                }
                .onFailure {
                    if (it !is UserCanceledBiometricsException) {
                        if (it is BiometricAuthenticationFailedException) {
                            intruderWarningCaptureService.captureWrongBiometrics()
                                .onFailure { error ->
                                    Timber.e(error, "Failed to capture intruder warning")
                                }
                        }
                        Dialogs.showLongToast(
                            context = fragment.requireContext(),
                            message = resources.getString(R.string.biometric_unlock_error),
                        )
                    }
                }
        }
    }
}
