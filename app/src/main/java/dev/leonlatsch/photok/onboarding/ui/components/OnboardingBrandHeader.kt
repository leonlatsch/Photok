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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import dev.leonlatsch.photok.onboarding.ui.OnboardingRoute
import dev.leonlatsch.photok.pro.navigation.ProRoute
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.theme.Dimens
import dev.leonlatsch.photok.ui.uicomponents.AppName

private const val STEP_COUNT = 5

fun NavKey.step(): Int? = when (this) {
    OnboardingRoute.Welcome -> null
    OnboardingRoute.SelectStart -> 1
    OnboardingRoute.SetupPassword -> 2
    OnboardingRoute.BiometricUnlock -> 3
    OnboardingRoute.RecoveryPhraseSetup -> 4
    OnboardingRoute.AddFirstPhotos -> 5
    OnboardingRoute.Done -> null
    ProRoute.OnboardingOffer -> null
    else -> null
}

@Composable
fun OnboardingBrandHeader(step: Int, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
        modifier = modifier
            .padding(horizontal = Dimens.spacingLarge)
            .padding(top = Dimens.spacingSmall, bottom = Dimens.spacingXLarge),
    ) {
        AppName(
            color = LocalContentColor.current,
        )

        SegmentedProgress(step = step)
    }
}

@Composable
private fun SegmentedProgress(step: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXSmall),
        modifier = Modifier.fillMaxWidth(),
    ) {
        for (segment in 1..STEP_COUNT) {
            val color by animateColorAsState(
                targetValue = if (segment <= step) {
                    LocalContentColor.current
                } else {
                    LocalContentColor.current.copy(alpha = 0.25f)
                },
                animationSpec = tween(durationMillis = 400),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun OnboardingBrandHeaderPreview() {
    AppTheme {
        Surface(
            color = colorScheme.primary,
            contentColor = colorScheme.surface,
        ) {
            OnboardingBrandHeader(step = 2)
        }
    }
}
