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

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.onboarding.ui.OnboardingRoute
import dev.leonlatsch.photok.pro.navigation.ProRoute
import dev.leonlatsch.photok.ui.theme.AppTheme
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

@DrawableRes
private fun stepIcon(step: Int): Int = when (step) {
    1 -> R.drawable.ic_lock
    2 -> R.drawable.ic_password
    3 -> R.drawable.ic_fingerprint
    4 -> R.drawable.ic_key
    5 -> R.drawable.ic_image
    else -> R.drawable.ic_star_filled
}

private fun stepTitle(step: Int): String = when (step) {
    1 -> "Where to start?"
    2 -> "Your Password"
    3 -> "Unlock with a touch"
    4 -> "Recovery Phrase"
    5 -> "Import photos"
    else -> "Photok Pro"
}

@Composable
fun OnboardingBrandHeader(step: Int, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingLarge),
        modifier = modifier
            .padding(horizontal = Dimens.spacingLarge)
            .padding(top = Dimens.spacingSmall, bottom = Dimens.spacingXLarge),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppName(
                fontSize = 32.sp,
                color = LocalContentColor.current,
            )
            Spacer(Modifier.weight(1f))
            StepCounter(step = step)
        }

        StepTitle(step = step)

        SegmentedProgress(step = step)
    }
}

@Composable
private fun StepCounter(step: Int) {
    Surface(
        shape = CircleShape,
        color = LocalContentColor.current.copy(alpha = 0.15f),
        contentColor = LocalContentColor.current,
    ) {
        AnimatedContent(
            targetState = step,
            transitionSpec = { stepTransition(forward = targetState > initialState) },
            modifier = Modifier.padding(horizontal = Dimens.spacingSmall, vertical = Dimens.spacingXSmall),
        ) { targetStep ->
            Text(
                text = "Step $targetStep",
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun StepTitle(step: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(LocalContentColor.current.copy(alpha = 0.15f)),
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = { stepTransition(forward = targetState > initialState) },
            ) { targetStep ->
                Icon(
                    painter = painterResource(stepIcon(targetStep)),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        AnimatedContent(
            targetState = step,
            transitionSpec = { stepTransition(forward = targetState > initialState) },
        ) { targetStep ->
            Text(
                text = stepTitle(targetStep),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun stepTransition(forward: Boolean) =
    if (forward) {
        (slideInVertically { it / 2 } + fadeIn()) togetherWith (slideOutVertically { -it / 2 } + fadeOut())
    } else {
        (slideInVertically { -it / 2 } + fadeIn()) togetherWith (slideOutVertically { it / 2 } + fadeOut())
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
