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

package dev.leonlatsch.photok.forwarddialer

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.forwarddialer.usecase.IsAirplaneModeOnUseCase
import dev.leonlatsch.photok.other.SingleLiveEvent
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.settings.data.Config.Companion.TIMESTAMP_LAST_RECOVERY_START_DEFAULT
import javax.inject.Inject

const val RECOVERY_MENU_MILLIS_THRESHOLD = 5000L

@HiltViewModel
class ForwardDialerViewModel @Inject constructor(
    private val isAirplaneModeOn: IsAirplaneModeOnUseCase,
    private val config: Config
) : ViewModel() {

    val navigationEvent = SingleLiveEvent<ForwardDialerNavigator.NavigationEvent>()

    fun evaluateNavigation() = if (isAirplaneModeOn()) {
        val now = System.currentTimeMillis()
        val lastRecoveryStart = config.timestampLastRecoveryStart

        val millisSinceLastRecoveryStart = now - lastRecoveryStart

        if (millisSinceLastRecoveryStart < RECOVERY_MENU_MILLIS_THRESHOLD) {
            navigationEvent.value = ForwardDialerNavigator.NavigationEvent.OpenRecoveryMenu
            config.timestampLastRecoveryStart = TIMESTAMP_LAST_RECOVERY_START_DEFAULT
        } else {
            config.timestampLastRecoveryStart = now

            // TODO: Show toast
            navigateToDialer()
        }

    } else {
        navigateToDialer()
    }

    private fun navigateToDialer() {
        navigationEvent.value = ForwardDialerNavigator.NavigationEvent.ForwardToDialer
    }
}