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

package dev.leonlatsch.photok.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.pro.navigation.ProRoute
import dev.leonlatsch.photok.pro.purchases.PurchaseService
import dev.leonlatsch.photok.telemetry.domain.Signal
import dev.leonlatsch.photok.telemetry.domain.TelemetryService
import javax.inject.Inject

@HiltViewModel
class AddFirstPhotosViewModel @Inject constructor(
    private val purchaseService: PurchaseService,
    private val telemetryService: TelemetryService,
) : ViewModel() {

    fun onImportClicked() {
        telemetryService.signal(Signal.OnboardingAddPhotosImportClicked)
    }


    fun nextRoute(): NavKey = if (BuildConfig.PLAY && !purchaseService.get()) {
        ProRoute.OnboardingOffer
    } else {
        OnboardingRoute.Done
    }
}
