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

package dev.leonlatsch.photok.encryption.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.encryption.domain.VaultService
import dev.leonlatsch.photok.encryption.domain.models.CreateRequest
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionType
import dev.leonlatsch.photok.other.extensions.areBiometricsAvailable
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.uicomponnets.Dialogs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BiometricUnlockViewModel @Inject constructor(
    private val vaultService: VaultService,
    private val sessionRepository: SessionRepository,
    private val config: Config,
) : ViewModel() {

    val enabled = config.valuesFlow.map {
        config.biometricAuthenticationEnabled
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), config.biometricAuthenticationEnabled)

    fun enableBiometricUnlock(activity: AppCompatActivity) = viewModelScope.launch {
        if (activity.areBiometricsAvailable().not()) {
            Dialogs.showLongToast(
                activity,
                activity.getString(R.string.settings_security_biometric_not_available),
            )
            return@launch
        }

        val result = runCatching {
            val session = requireNotNull(sessionRepository.get())
            vaultService.create(CreateRequest.Biometric(session, activity))
        }

        result.onFailure {
            if (it !is UserCanceledBiometricsException) {
                Dialogs.showLongToast(
                    activity,
                    it.localizedMessage ?: activity.getString(R.string.common_error),
                )
            }
        }

        config.biometricAuthenticationEnabled = result.isSuccess
    }

    fun disableBiometricUnlock() = viewModelScope.launch {
        vaultService.reset(VaultProtectionType.Biometric)
        config.biometricAuthenticationEnabled = false
    }
}