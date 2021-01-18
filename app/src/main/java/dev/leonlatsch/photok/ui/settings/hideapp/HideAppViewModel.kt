/*
 *   Copyright 2020-2021 Leon Latsch
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

package dev.leonlatsch.photok.ui.settings.hideapp

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.ui.components.bindings.ObservableViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the HideAppDialog. Holds a state.
 *
 * @since 1.2.0
 * @author Leon Latsch
 */
class HideAppViewModel @Inject constructor(
    private val app: Application
) : ObservableViewModel(app) {

    @get:Bindable
    var isAppHidden: Boolean = false
        set(value) {
            field = value
            notifyChange(BR.appHidden, value)
        }

    fun disableMainComponent() = viewModelScope.launch {
        app.packageManager.setComponentEnabledSetting(
            MAIN_LAUNCHER_COMPONENT,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        app.packageManager.setComponentEnabledSetting(
            STEALTH_LAUNCHER_COMPONENT,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        updateState()
    }

    fun enableMainComponent() = viewModelScope.launch {
        app.packageManager.setComponentEnabledSetting(
            MAIN_LAUNCHER_COMPONENT,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        app.packageManager.setComponentEnabledSetting(
            STEALTH_LAUNCHER_COMPONENT,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        updateState()
    }

    fun updateState() {
        val enabledSetting = app.packageManager.getComponentEnabledSetting(MAIN_LAUNCHER_COMPONENT)
        isAppHidden = enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    }

    companion object {
        private val MAIN_LAUNCHER_COMPONENT =
            ComponentName("dev.leonlatsch.photok", "dev.leonlatsch.photok.MainLauncher")

        private val STEALTH_LAUNCHER_COMPONENT =
            ComponentName("dev.leonlatsch.photok", "dev.leonlatsch.photok.StealthLauncher")
    }
}