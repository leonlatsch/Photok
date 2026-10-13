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

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface OnboardingRoute : NavKey {

    @Serializable
    data object Welcome : OnboardingRoute

    @Serializable
    data object SelectStart : OnboardingRoute

    @Serializable
    data object SetupPassword : OnboardingRoute

    @Serializable
    data object BiometricUnlock : OnboardingRoute

    @Serializable
    data object RecoveryPhraseSetup : OnboardingRoute

    @Serializable
    data object AddFirstPhotos : OnboardingRoute

    @Serializable
    data object Done : OnboardingRoute
}

fun OnboardingRoute.stepKey(): String = when (this) {
    OnboardingRoute.Welcome -> "welcome"
    OnboardingRoute.SelectStart -> "selectStart"
    OnboardingRoute.SetupPassword -> "setupPassword"
    OnboardingRoute.BiometricUnlock -> "biometricUnlock"
    OnboardingRoute.RecoveryPhraseSetup -> "recoveryPhraseSetup"
    OnboardingRoute.AddFirstPhotos -> "addFirstPhotos"
    OnboardingRoute.Done -> "done"
}
