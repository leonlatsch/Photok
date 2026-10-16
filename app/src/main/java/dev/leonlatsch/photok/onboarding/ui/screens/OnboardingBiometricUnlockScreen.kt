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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.core.R
import dev.leonlatsch.photok.encryption.ui.BiometricUnlockViewModel
import dev.leonlatsch.photok.navigation.LocalNavigator
import dev.leonlatsch.photok.onboarding.ui.OnboardingRoute
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingNexButtonBar
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingPageSubtitle
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
        onEnabledChanged = { newEnabled ->
            if (newEnabled) {
                activity?.let { viewModel.enableBiometricUnlock(it) }
            } else {
                viewModel.disableBiometricUnlock()
            }
        },

    )
}

@Composable
private fun OnboardingBiometricUnlockContent(
    enabled: Boolean,
    onEnabledChanged: (Boolean) -> Unit,
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

                OnboardingNexButtonBar(text = "Enable biometric unlock") {
                    onEnabledChanged(true)
                }
            }
        }
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
            modifier = Modifier
                .padding(contentPadding)
                .padding(Dimens.spacingLarge)
        ) {
            OnboardingPageSubtitle(
                text = "Use your fingerprint or face instead of your password."
            )

            Surface(
                shape = RoundedCornerShape(18.dp),
                tonalElevation = 2.dp,
            ) {
                Column(
                    modifier = Modifier
                        .clickable {
                            onEnabledChanged(!enabled)
                        }
                        .padding(Dimens.spacingMedium)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = colorScheme.primary,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_fingerprint),
                                contentDescription = null,
                                modifier = Modifier.padding(Dimens.spacingSmall)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Biometric Unlock",
                                style = typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                            )

                            Text(
                                text = "You can change this in the settings later",
                                color = colorScheme.outline,
                                style = typography.labelLarge
                            )
                        }

                        Switch(
                            checked = enabled,
                            onCheckedChange = {
                                onEnabledChanged(it)
                            },
                        )
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun OnboardingBiometricUnlockScreenPreview() {
    AppTheme {
        OnboardingBiometricUnlockContent(
            enabled = false,
            onEnabledChanged = {},
        )
    }
}
