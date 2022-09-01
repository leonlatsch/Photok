/*
 *   Copyright 2020-2022 Leon Latsch
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

package dev.leonlatsch.photok.settings.ui.hideapp.usecase

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import javax.inject.Inject

private val MAIN_LAUNCHER_COMPONENT =
    ComponentName("dev.leonlatsch.photok", "dev.leonlatsch.photok.MainLauncher")

private val STEALTH_LAUNCHER_COMPONENT =
    ComponentName("dev.leonlatsch.photok", "dev.leonlatsch.photok.StealthLauncher")

class ToggleMainComponentUseCase @Inject constructor(
    private val app: Application
) {

    operator fun invoke() {
        if (isMainComponentDisabled()) {
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
        } else {
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
        }
    }

    fun isMainComponentDisabled(): Boolean {
        val enabledSetting = app.packageManager.getComponentEnabledSetting(
            MAIN_LAUNCHER_COMPONENT
        )
        return enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    }
}