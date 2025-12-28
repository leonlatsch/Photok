/*
 *   Copyright 2020-2024 Leon Latsch
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

package dev.leonlatsch.photok.settings.ui.compose

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.security.biometric.BiometricUnlock
import dev.leonlatsch.photok.security.biometric.UserCanceledBiometricsException
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.settings.domain.models.SettingsEnum
import dev.leonlatsch.photok.uicomponnets.Dialogs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val screenConfig: PreferenceScreenConfig = PreferenceScreenConfig(PreferenceScreenConfigContent),
    val preferencesValues: Map<String, *> = emptyMap<String, String>(),
)

sealed interface SettingsUiEvent {
    data class ToggleSwitch(val key: String, val value: Boolean) : SettingsUiEvent
    data class SetEnumValue(val key: String, val value: SettingsEnum) : SettingsUiEvent

    data class ToggleBiometricUnlock(val value: Boolean, val fragment: Fragment) : SettingsUiEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val config: Config,
    private val biometricUnlock: BiometricUnlock,
) : ViewModel() {
    val uiState = config.valuesFlow.map {  values ->
        SettingsUiState(
            screenConfig = PreferenceScreenConfig(PreferenceScreenConfigContent),
            preferencesValues = values,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SettingsUiState(preferencesValues = config.values))

    fun handleUiEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.ToggleSwitch -> config.putBoolean(event.key, event.value)
            is SettingsUiEvent.SetEnumValue -> config.putString(event.key, event.value.value)
            is SettingsUiEvent.ToggleBiometricUnlock -> {
                if (!event.value) {
                    viewModelScope.launch { biometricUnlock.reset() }
                    config.biometricAuthenticationEnabled = false
                    return
                }

                val context = event.fragment.context ?: return

                if (!biometricUnlock.areBiometricsAvailable()) {
                    Dialogs.showLongToast(
                        context,
                        context.getString(R.string.settings_security_biometric_not_available),
                    )
                    return
                }

                viewModelScope.launch {

                    val result = biometricUnlock.setup(event.fragment)

                    result.onFailure {
                        if (it !is UserCanceledBiometricsException) {
                            Dialogs.showLongToast(
                                context,
                                it.localizedMessage ?: context.getString(R.string.common_error),
                            )
                        }
                    }

                    config.biometricAuthenticationEnabled = result.isSuccess
                }
            }
        }
    }
}