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

package dev.leonlatsch.photok.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.settings.ui.compose.LocalConfig
import dev.leonlatsch.photok.settings.ui.compose.SettingsScreen
import dev.leonlatsch.photok.ui.LocalFragment
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    @Inject
    lateinit var config: Config

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CompositionLocalProvider(
                    LocalFragment provides this@SettingsFragment,
                    LocalConfig provides config,
                ) {
                    SettingsScreen()
                }
            }
        }
    }

    companion object {
        const val KEY_ACTION_RESET = "action_reset_safe"
        const val KEY_ACTION_CHANGE_PASSWORD = "action_change_password"
        const val KEY_ACTION_CHECK_PASSWORD = "action_check_password"
        const val KEY_ACTION_HIDE_APP = "action_hide_app"
        const val KEY_ACTION_BACKUP = "action_backup_safe"
        const val KEY_ACTION_FEEDBACK = "action_feedback"
        const val KEY_ACTION_DONATE = "action_donate"
        const val KEY_ACTION_SOURCECODE = "action_sourcecode"
        const val KEY_ACTION_CREDITS = "action_credits"
        const val KEY_ACTION_ABOUT = "action_about"
    }
}