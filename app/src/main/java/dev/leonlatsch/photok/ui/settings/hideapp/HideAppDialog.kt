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

import android.content.ComponentName
import android.content.pm.PackageManager
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.DialogHideAppBinding
import dev.leonlatsch.photok.ui.components.BindableDialogFragment
import dev.leonlatsch.photok.ui.components.Dialogs

@AndroidEntryPoint
class HideAppDialog : BindableDialogFragment<DialogHideAppBinding>(R.layout.dialog_hide_app) {

    override fun bind(binding: DialogHideAppBinding) {
        super.bind(binding)
        binding.context = this
    }

    fun hideApp() {
        Dialogs.showConfirmDialog(requireContext(), "DO u want to hide???") { _, _ ->
            requireActivity().packageManager.setComponentEnabledSetting(
                MAIN_LAUNCHER_COMPONENT,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
            requireActivity().packageManager.setComponentEnabledSetting(
                STEALTH_LAUNCHER_COMPONENT,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }

    fun showApp() {
        Dialogs.showConfirmDialog(requireContext(), "DO u want to show???") { _, _ ->
            requireActivity().packageManager.setComponentEnabledSetting(
                MAIN_LAUNCHER_COMPONENT,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            requireActivity().packageManager.setComponentEnabledSetting(
                STEALTH_LAUNCHER_COMPONENT,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }

    companion object {
        private val MAIN_LAUNCHER_COMPONENT =
            ComponentName("dev.leonlatsch.photok", "dev.leonlatsch.photok.MainLauncher")

        private val STEALTH_LAUNCHER_COMPONENT =
            ComponentName("dev.leonlatsch.photok", "dev.leonlatsch.photok.StealthLauncher")
    }
}