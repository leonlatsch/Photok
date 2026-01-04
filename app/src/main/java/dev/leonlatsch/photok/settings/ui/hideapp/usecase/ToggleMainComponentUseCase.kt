


package dev.leonlatsch.photok.settings.ui.hideapp.usecase

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import dev.leonlatsch.photok.BuildConfig
import javax.inject.Inject

private val MAIN_LAUNCHER_COMPONENT =
    ComponentName(BuildConfig.APPLICATION_ID, "dev.leonlatsch.photok.MainLauncher")

private val STEALTH_LAUNCHER_COMPONENT =
    ComponentName(BuildConfig.APPLICATION_ID, "dev.leonlatsch.photok.StealthLauncher")

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

package dev.leonlatsch.photok.settings.ui.hideapp.usecase

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import dev.leonlatsch.photok.BuildConfig
import javax.inject.Inject

private val MAIN_LAUNCHER_COMPONENT =
    ComponentName(BuildConfig.APPLICATION_ID, "dev.leonlatsch.photok.MainLauncher")

private val STEALTH_LAUNCHER_COMPONENT =
    ComponentName(BuildConfig.APPLICATION_ID, "dev.leonlatsch.photok.StealthLauncher")

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