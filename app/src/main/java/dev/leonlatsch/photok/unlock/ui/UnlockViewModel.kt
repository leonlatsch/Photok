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

import android.app.Application
import android.content.res.Resources
import androidx.databinding.Bindable
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BR
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
import dev.leonlatsch.photok.other.extensions.empty
import dev.leonlatsch.photok.pro.domain.PasswordAttemptsResult
import dev.leonlatsch.photok.pro.domain.PasswordAttemptsUseCase
import dev.leonlatsch.photok.pro.intruderwarnings.domain.IntruderWarningCaptureService
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.uicomponnets.Dialogs
import dev.leonlatsch.photok.uicomponnets.bindings.ObservableViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
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
    app: Application,
    private val config: Config,
    private val resources: Resources,
    private val vaultService: VaultService,
    private val sessionRepository: SessionRepository,
    private val legacyEncryptionMigrator: LegacyEncryptionMigrator,
    private val legacyEncryption: LegacyEncryption,
    private val passwordAttemptsUseCase: PasswordAttemptsUseCase,
    private val intruderWarningCaptureService: IntruderWarningCaptureService,
    private val eraseVaultDataUseCase: EraseVaultDataUseCase,
) : ObservableViewModel(app) {

    @Bindable
    var password: String = String.empty
        set(value) {
            field = value
            notifyChange(BR.password, value)
        }

    val uiState: MutableStateFlow<UnlockUiState> = MutableStateFlow(UnlockUiState.Initial)

    private var lockoutJob: Job? = null

    init {
        val lockedUntil = passwordAttemptsUseCase.currentLockout()
        if (lockedUntil > System.currentTimeMillis()) {
            lockUntil(lockedUntil)
        }
    }

    /**
     * Enters the lockout state and leaves it again once [lockedUntil] has passed.
     */
    private fun lockUntil(lockedUntil: Long) {
        uiState.update { UnlockUiState.Locked(lockedUntil) }

        lockoutJob?.cancel()
        lockoutJob = viewModelScope.launch {
            while (System.currentTimeMillis() < lockedUntil) {
                delay(1.seconds)
            }
            dismissLockout()
        }
    }

    /**
     * Tries to unlock the safe.
     * Compares [password] to saved hash.
     * Updates UnlockState.
     * Called by ui.
     */
    fun unlockWithPassword() {
        uiState.update { UnlockUiState.Loading }

        viewModelScope.launch {
            try {
                vaultService.unlock(UnlockRequest.Password(password))
                    .onSuccess { session ->
                        passwordAttemptsUseCase.onSuccessfulUnlock()
                        sessionRepository.set(session)

                        if (legacyEncryptionMigrator.migrationNeeded() || config.legacyCurrentlyMigrating) {
                            val legacySession = legacyEncryption.obtainSession(password)
                            legacyEncryptionMigrator.initialize(legacySession)

                            uiState.update { UnlockUiState.StartLegacyMigration }
                        } else if (!vaultService.isSetup(VaultProtectionType.RecoveryPhrase)) {
                            vaultService.create(CreateRequest.RecoveryPhrase(session, Bip39WordCount.Twelve))
                            uiState.update { UnlockUiState.ShowRecoveryPhrase }
                        } else {
                            uiState.update { UnlockUiState.Unlocked }
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
                            PasswordAttemptsResult.None -> uiState.update { UnlockUiState.PasswordError }
                            PasswordAttemptsResult.Erased -> {
                                viewModelScope.launch {
                                    eraseVaultDataUseCase()
                                }
                                uiState.update { UnlockUiState.PasswordError }
                            }
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e)
                uiState.update { UnlockUiState.Error }
            }
        }
    }

    fun dismissLockout() {
        passwordAttemptsUseCase.onSuccessfulUnlock()
        uiState.update { UnlockUiState.Initial }
    }

    fun unlockWithBiometric(fragment: Fragment) {
        // A bruteforce lockout must not be skippable by falling back to biometrics.
        if (uiState.value is UnlockUiState.Locked) return

        viewModelScope.launch {
            vaultService.unlock(UnlockRequest.Biometric(fragment))
                .onSuccess { session ->
                    passwordAttemptsUseCase.onSuccessfulUnlock()
                    sessionRepository.set(session)
                    uiState.update { UnlockUiState.Unlocked }
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
