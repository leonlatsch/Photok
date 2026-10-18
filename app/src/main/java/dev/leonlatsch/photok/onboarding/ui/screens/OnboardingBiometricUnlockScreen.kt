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

package dev.leonlatsch.photok.onboarding.ui.screens

import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.ui.BiometricUnlockViewModel
import dev.leonlatsch.photok.navigation.LocalNavigator
import dev.leonlatsch.photok.onboarding.ui.OnboardingCardWindowInsets
import dev.leonlatsch.photok.onboarding.ui.OnboardingRoute
import dev.leonlatsch.photok.onboarding.ui.OnboardingUiEvent
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingNexButtonBar
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingPageSubtitle
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingPageTitle
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.theme.Dimens
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Composable
fun OnboardingBiometricUnlockScreen(
    handleUiEvent: (OnboardingUiEvent) -> Unit,
) {
    val activity = LocalActivity.current as? AppCompatActivity

    val viewModel: BiometricUnlockViewModel = hiltViewModel()
    val enabled by viewModel.enabled.collectAsStateWithLifecycle()

    OnboardingBiometricUnlockContent(
        enabled = enabled,
        onEnableButtonClick = {
            handleUiEvent(OnboardingUiEvent.BiometricEnableButtonClicked)
            activity?.let { viewModel.enableBiometricUnlock(it) }
        },
        onGlyphClick = {
            handleUiEvent(OnboardingUiEvent.BiometricGlyphClicked)
            activity?.let { viewModel.enableBiometricUnlock(it) }
        },
    )
}

@Composable
private fun OnboardingBiometricUnlockContent(
    enabled: Boolean,
    onEnableButtonClick: () -> Unit,
    onGlyphClick: () -> Unit,
) {
    val navigator = LocalNavigator.current

    LaunchedEffect(enabled) {
        if (enabled) {
            delay(1.seconds)
            navigator.navigate(OnboardingRoute.RecoveryPhraseSetup)
        }
    }

    Scaffold(
        contentWindowInsets = OnboardingCardWindowInsets,
        bottomBar = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.navigationBarsPadding()
            ) {
                TextButton(
                    onClick = {
                        navigator.navigate(OnboardingRoute.RecoveryPhraseSetup)
                    },
                    enabled = !enabled,
                ) {
                    Text(stringResource(R.string.onboarding_not_now))
                }

                OnboardingNexButtonBar(
                    text = stringResource(R.string.onboarding_biometric_enable),
                    enabled = !enabled,
                    onNext = onEnableButtonClick,
                )
            }
        }
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
            modifier = Modifier
                .padding(contentPadding)
                .padding(Dimens.spacingLarge)
        ) {
            OnboardingPageTitle(text = stringResource(R.string.onboarding_biometric_title))

            OnboardingPageSubtitle(
                text = stringResource(R.string.onboarding_biometric_subtitle)
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                BiometricGlyph(
                    done = enabled,
                    onClick = onGlyphClick,
                )
            }
        }
    }
}

@Composable
private fun BiometricGlyph(
    done: Boolean,
    onClick: () -> Unit,
) {
    var clicked by remember { mutableStateOf(false) }
    val pulse = remember { Animatable(1f) }

    LaunchedEffect(done) {
        if (done) {
            pulse.animateTo(1f)
            return@LaunchedEffect
        }
        while (!clicked) {
            delay(2500.milliseconds)
            pulse.animateTo(1.05f, tween(durationMillis = 280, easing = FastOutSlowInEasing))
            pulse.animateTo(1f, spring(dampingRatio = 0.3f, stiffness = Spring.StiffnessLow))
        }
    }

    Surface(
        shape = CircleShape,
        color = colorScheme.primary,
        contentColor = colorScheme.onPrimary,
        modifier = Modifier
            .graphicsLayer {
                scaleX = pulse.value
                scaleY = pulse.value
            }
            .clip(CircleShape)
            .clickable(enabled = !done) {
                clicked = true
                onClick()
            }
    ) {
        AnimatedContent(
            targetState = done,
            transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.8f)) togetherWith fadeOut() },
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(32.dp),
        ) { shownEnabled ->
            Icon(
                painter = painterResource(if (shownEnabled) R.drawable.ic_check_circle else R.drawable.ic_fingerprint),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun OnboardingBiometricUnlockScreenPreview() {
    AppTheme {
        OnboardingBiometricUnlockContent(
            enabled = false,
            onEnableButtonClick = {},
            onGlyphClick = {},
        )
    }
}
