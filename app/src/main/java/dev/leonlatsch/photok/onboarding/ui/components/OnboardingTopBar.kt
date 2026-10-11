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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.navigation.LocalNavigator
import dev.leonlatsch.photok.onboarding.ui.OnboardingRoute
import dev.leonlatsch.photok.ui.theme.Dimens

private const val STEP_COUNT = 6
private val TOP_BAR_HEIGHT = 64.dp

fun OnboardingRoute.step(): Int? = when (this) {
    OnboardingRoute.Welcome -> null
    OnboardingRoute.SelectStart -> 1
    OnboardingRoute.SetupPassword -> 2
    OnboardingRoute.BiometricUnlock -> 3
    OnboardingRoute.RecoveryPhraseSetup -> 4
}

@Composable
fun OnboardingTopBar(step: Int) {
    val navigator = LocalNavigator.current
    val progress by animateFloatAsState(step.toFloat() / STEP_COUNT)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(TOP_BAR_HEIGHT)
            .padding(end = Dimens.spacingLarge),
    ) {
        IconButton(onClick = navigator::goBack) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = null,
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "$step of $STEP_COUNT",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun OnboardingTopBarSpacer() {
    Spacer(
        modifier = Modifier
            .statusBarsPadding()
            .height(TOP_BAR_HEIGHT),
    )
}
