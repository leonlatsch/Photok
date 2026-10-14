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

package dev.leonlatsch.photok.onboarding.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.onboarding.ui.OnboardingRoute
import dev.leonlatsch.photok.pro.navigation.ProRoute
import dev.leonlatsch.photok.ui.theme.Dimens
import dev.leonlatsch.photok.ui.uicomponents.AppName

private val STEP_COUNT = if (BuildConfig.PLAY) 6 else 5

fun NavKey.step(): Int? = when (this) {
    OnboardingRoute.Welcome -> null
    OnboardingRoute.SelectStart -> 1
    OnboardingRoute.SetupPassword -> 2
    OnboardingRoute.BiometricUnlock -> 3
    OnboardingRoute.RecoveryPhraseSetup -> 4
    OnboardingRoute.AddFirstPhotos -> 5
    OnboardingRoute.Done -> null
    ProRoute.OnboardingOffer -> 6
    else -> null
}

@Composable
fun OnboardingBrandHeader(step: Int, modifier: Modifier = Modifier) {
    val progress by animateFloatAsState(step.toFloat() / STEP_COUNT)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        AppName(
            fontSize = 48.sp,
            color = LocalContentColor.current
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = Dimens.spacingLarge),
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = colorScheme.surface,
                trackColor = colorScheme.surface.copy(alpha = 0.3f),
            )
        }
    }
}
