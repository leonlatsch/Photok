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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BaseApplication
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.security.PasswordManager
import dev.leonlatsch.photok.security.biometric.BiometricUnlock
import dev.leonlatsch.photok.security.biometric.UserCanceledBiometricsException
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.settings.domain.Preference
import dev.leonlatsch.photok.settings.domain.PreferenceScreenConfig
import dev.leonlatsch.photok.settings.domain.PreferenceScreenConfigContent
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
    data class OnPreferenceClick(val preference: Preference, val value: Any?) : SettingsUiEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val config: Config,
    private val biometricUnlock: BiometricUnlock,
    private val app: Application,
    private val photoRepository: PhotoRepository,
    private val albumRepository: AlbumRepository,
    private val passwordManager: PasswordManager,
) : ViewModel() {


    val uiState = config.valuesFlow.map {  values ->
        SettingsUiState(
            screenConfig = PreferenceScreenConfig(PreferenceScreenConfigContent),
            preferencesValues = values,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SettingsUiState(preferencesValues = config.values))

    fun handleUiEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.OnPreferenceClick -> {
                val proceed = callbacks[event.preference.key]?.invoke(event.value) ?: true

                if (!proceed) {
                    return
                }

                when (event.preference) {

                    is Preference.Enum<*> -> config.putString(event.preference.key, (event.value as SettingsEnum).value)
                    is Preference.Switch -> config.putBoolean(event.preference.key, event.value as Boolean)
                    is Preference.Simple -> Unit
                }
            }
        }
    }


    fun registerPreferenceCallback(key: String, callback: (value: Any?) -> Boolean) {
        callbacks[key] = callback
    }

    private val callbacks: MutableMap<String, (value: Any?) -> Boolean> = mutableMapOf()

    fun onBiometricUnlockChanged(value: Any?, fragment: Fragment): Boolean {
        value as Boolean

        if (!value) {
            viewModelScope.launch { biometricUnlock.reset() }
            config.biometricAuthenticationEnabled = false
            return false
        }

        val context = fragment.context ?: return false

        if (!biometricUnlock.areBiometricsAvailable()) {
            Dialogs.showLongToast(
                context,
                context.getString(R.string.settings_security_biometric_not_available),
            )
            return false
        }

        viewModelScope.launch {

            val result = biometricUnlock.setup(fragment)

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

        return false
    }

    fun resetComponents() = viewModelScope.launch {
        val allPhotos = photoRepository.findAllPhotosByImportDateDesc()
        for (photo in allPhotos) {
            photoRepository.deleteInternalPhotoData(photo)
        }
        photoRepository.deleteAll()
        albumRepository.deleteAll()
        albumRepository.unlinkAll()

        passwordManager.resetPassword()
        (app as BaseApplication).lockApp()
    }
}