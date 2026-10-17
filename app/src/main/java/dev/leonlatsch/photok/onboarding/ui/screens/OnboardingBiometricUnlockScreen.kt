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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.ui.BiometricUnlockViewModel
import dev.leonlatsch.photok.navigation.LocalNavigator
import dev.leonlatsch.photok.onboarding.ui.OnboardingRoute
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingNexButtonBar
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingPageSubtitle
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingPageTitle
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.theme.Dimens
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun OnboardingBiometricUnlockScreen() {
    val activity = LocalActivity.current as? AppCompatActivity

    val viewModel: BiometricUnlockViewModel = hiltViewModel()
    val enabled by viewModel.enabled.collectAsStateWithLifecycle()

    OnboardingBiometricUnlockContent(
        enabled = enabled,
        onEnable = {
            activity?.let { viewModel.enableBiometricUnlock(it) }
        },
    )
}

@Composable
private fun OnboardingBiometricUnlockContent(
    enabled: Boolean,
    onEnable: () -> Unit,
) {
    val navigator = LocalNavigator.current

    LaunchedEffect(enabled) {
        if (enabled) {
            delay(1.seconds)
            navigator.navigate(OnboardingRoute.RecoveryPhraseSetup)
        }
    }

    Scaffold(
        bottomBar = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.navigationBarsPadding()
            ) {
                TextButton(
                    onClick = {
                        navigator.navigate(OnboardingRoute.RecoveryPhraseSetup)
                    }
                ) {
                    Text("Not now")
                }

                OnboardingNexButtonBar(
                    text = "Enable biometric unlock",
                    enabled = !enabled,
                    onNext = onEnable,
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
            OnboardingPageTitle(text = "Unlock with a touch")

            OnboardingPageSubtitle(
                text = "Unlock Photok with your fingerprint or face instead of typing your password. You can change this later in the settings."
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                BiometricGlyph(
                    done = enabled,
                    onClick = onEnable,
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
    Surface(
        shape = CircleShape,
        color = colorScheme.primary,
        contentColor = colorScheme.onPrimary,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(
                enabled = !done,
                onClick = onClick,
            )
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
            onEnable = {},
        )
    }
}
