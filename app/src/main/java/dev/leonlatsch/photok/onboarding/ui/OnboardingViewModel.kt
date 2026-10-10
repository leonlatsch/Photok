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

package dev.leonlatsch.photok.onboarding.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.telemetry.domain.Signal
import dev.leonlatsch.photok.telemetry.domain.TelemetryService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val config: Config,
    private val telemetryService: TelemetryService,
) : ViewModel() {

    private val navigationEventsChannel = Channel<OnboardingNavigationEvent>()
    val navigationEvents = navigationEventsChannel.receiveAsFlow()

    fun handleUiEvent(event: OnboardingUiEvent) {
        when (event) {
            OnboardingUiEvent.Finish -> finish()
        }
    }

    private fun finish() {
        telemetryService.signal(Signal.OnboardingFinished)
        config.systemFirstStart = false
        navigationEventsChannel.trySend(OnboardingNavigationEvent.Finished)
    }
}
