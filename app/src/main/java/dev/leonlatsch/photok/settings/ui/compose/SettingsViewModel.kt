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

package dev.leonlatsch.photok.settings.ui.compose

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.encryption.domain.ResetVaultUseCase
import dev.leonlatsch.photok.pro.purchases.PurchaseService
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.settings.domain.Preference
import dev.leonlatsch.photok.settings.domain.PreferenceScreenConfig
import dev.leonlatsch.photok.settings.domain.PrefsScreenConfig
import dev.leonlatsch.photok.settings.domain.models.SettingsEnum
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val screenConfig: PreferenceScreenConfig = PrefsScreenConfig,
    val preferencesValues: Map<String, *> = emptyMap<String, String>(),
    val proFeaturesActive: Boolean = false,
) {
}

sealed interface SettingsUiEvent {
    data class OnPreferenceClick(val preference: Preference, val value: Any?) : SettingsUiEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val config: Config,
    private val app: Application,
    private val resetVaultUseCase: ResetVaultUseCase,
    private val proFeaturesActive: PurchaseService,
) : ViewModel() {

    val uiState = combine(
        config.valuesFlow,
        proFeaturesActive.observe(),
    ) { values, proFeaturesActive ->
        SettingsUiState(
            screenConfig = PrefsScreenConfig,
            preferencesValues = values,
            proFeaturesActive = proFeaturesActive,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = SettingsUiState(
            preferencesValues = config.values,
            proFeaturesActive = proFeaturesActive.get(),
        ),
    )

    fun handleUiEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.OnPreferenceClick -> {
                val proceed = callbacks[event.preference.key]?.invoke(event.value) ?: true

                if (!proceed) {
                    return
                }

                when (event.preference) {

                    is Preference.Enum<*> -> config.putString(
                        event.preference.key,
                        (event.value as SettingsEnum).value
                    )

                    is Preference.Switch -> config.putBoolean(
                        event.preference.key,
                        event.value as Boolean
                    )

                    is Preference.Simple -> Unit
                }
            }
        }
    }


    fun registerPreferenceCallback(key: String, callback: (value: Any?) -> Boolean) {
        callbacks[key] = callback
    }

    private val callbacks: MutableMap<String, (value: Any?) -> Boolean> = mutableMapOf()

    fun resetApp() = viewModelScope.launch {
        resetVaultUseCase()
    }
}