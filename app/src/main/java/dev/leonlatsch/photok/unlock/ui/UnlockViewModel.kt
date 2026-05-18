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
import dev.leonlatsch.photok.encryption.domain.LegacyEncryption
import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.encryption.domain.VaultService
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import dev.leonlatsch.photok.encryption.migration.LegacyEncryptionMigrator
import dev.leonlatsch.photok.encryption.ui.UserCanceledBiometricsException
import dev.leonlatsch.photok.other.extensions.empty
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.uicomponnets.Dialogs
import dev.leonlatsch.photok.uicomponnets.bindings.ObservableViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

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
) : ObservableViewModel(app) {

    @Bindable
    var password: String = String.empty
        set(value) {
            field = value
            notifyChange(BR.password, value)
        }

    val unlockState: MutableStateFlow<UnlockState> = MutableStateFlow(UnlockState.Initial)

    /**
     * Tries to unlock the save.
     * Compares [password] to saved hash.
     * Updates UnlockState.
     * Called by ui.
     */
    fun unlockWithPassword() {
        unlockState.update { UnlockState.Loading }

        viewModelScope.launch {
            try {
                vaultService.unlock(UnlockRequest.Password(password))
                    .onSuccess { session ->
                        sessionRepository.set(session)

                        if (legacyEncryptionMigrator.migrationNeeded() || config.legacyCurrentlyMigrating) {
                            val legacySession = legacyEncryption.obtainSession(password)
                            legacyEncryptionMigrator.initialize(legacySession)

                            unlockState.update { UnlockState.StartLegacyMigration }
                        } else {
                            unlockState.update { UnlockState.Unlocked }
                        }
                    }
                    .onFailure {
                        unlockState.update { UnlockState.PasswordError }
                    }
            } catch (e: Exception) {
                Timber.e(e)
                unlockState.update { UnlockState.Error }
            }
        }
    }

    fun unlockWithBiometric(fragment: Fragment) {
        viewModelScope.launch {
            vaultService.unlock(UnlockRequest.Biometric(fragment))
                .onSuccess { session ->
                    sessionRepository.set(session)
                    unlockState.update { UnlockState.Unlocked }
                }
                .onFailure {
                    if (it !is UserCanceledBiometricsException) {
                        Dialogs.showLongToast(
                            context = fragment.requireContext(),
                            message = resources.getString(R.string.biometric_unlock_error),
                        )
                    }
                }
        }
    }

}