/*
 *   Copyright 2020-2026 Leon Latsch
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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.leonlatsch.photok.pro.passwordattempts.BruteforceProtectionSheet
import dev.leonlatsch.photok.pro.passwordattempts.PasswordAttemptsAction
import dev.leonlatsch.photok.pro.passwordattempts.PasswordAttemptsLimit
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.settings.domain.ProPreferences
import dev.leonlatsch.photok.settings.ui.SettingsFragment

@Composable
internal fun ProSettingsCallbacks(viewModel: SettingsViewModel) {
    val preferencesValues = LocalPreferencesValues.current
    var showBruteforceSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.registerPreferenceCallback(SettingsFragment.KEY_ACTION_BRUTEFORCE_PROTECTION) {
            showBruteforceSheet = true
            false
        }
    }

    if (showBruteforceSheet) {
        val rawLimit = preferencesValues[Config.SECURITY_MAX_PASSWORD_ATTEMPTS] as? String
        val currentLimit = PasswordAttemptsLimit.entries.find { it.value == rawLimit }
            ?: PasswordAttemptsLimit.Unlimited

        val rawAction = preferencesValues[Config.SECURITY_PASSWORD_ATTEMPTS_ACTION] as? String
        val currentAction = PasswordAttemptsAction.entries.find { it.value == rawAction }
            ?: PasswordAttemptsAction.Lockout

        BruteforceProtectionSheet(
            currentLimit = currentLimit,
            currentAction = currentAction,
            onLimitChanged = { limit ->
                viewModel.handleUiEvent(
                    SettingsUiEvent.OnPreferenceClick(ProPreferences.PasswordAttempts, limit)
                )
            },
            onActionChanged = { action ->
                viewModel.handleUiEvent(
                    SettingsUiEvent.OnPreferenceClick(ProPreferences.PasswordAttemptsAction, action)
                )
            },
            onDismiss = { showBruteforceSheet = false },
        )
    }
}
