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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingNexButtonBar
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingTopBarSpacer
import dev.leonlatsch.photok.ui.components.PasswordField
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.theme.Dimens

@Composable
fun OnboardingSetupPasswordScreen() {
    var password by rememberSaveable { mutableStateOf("") }
    var passwordConfirm by rememberSaveable { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            OnboardingTopBarSpacer()
        },
        bottomBar = {
            OnboardingNexButtonBar(enabled = password.isNotEmpty() && passwordConfirm.isNotEmpty()) {
                // TODO
            }
        }
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(horizontal = Dimens.spacingLarge)
                .padding(top = 100.dp)
        ) {
            Text(
                text = "Choose a password",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Medium,
            )

            Text(
                text = "You will need this to unlock Photok. Choose wisely and remember it.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Dimens.spacingXSmall))

            PasswordField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                imeAction = ImeAction.Next,
                modifier = Modifier.focusRequester(focusRequester)
            )

            AnimatedVisibility(
                visible = password.isNotEmpty(),
            ) {
                PasswordField(
                    value = passwordConfirm,
                    onValueChange = { passwordConfirm = it },
                    label = "Confirm password",
                    error = null,
                    onDone = {  },
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun OnboardingSetupPasswordScreenPreview() {
    AppTheme {
        OnboardingSetupPasswordScreen()
    }
}
