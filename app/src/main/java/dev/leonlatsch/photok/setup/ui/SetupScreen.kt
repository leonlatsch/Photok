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

package dev.leonlatsch.photok.setup.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.models.PasswordStrength
import dev.leonlatsch.photok.ui.components.PasswordField
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.uicomponents.AppName

@Composable
fun SetupScreen(viewModel: SetupViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AppTheme {
        SetupScreenContent(
            uiState = uiState,
            handleUiEvent = viewModel::handleUiEvent,
        )
    }
}

@Composable
private fun SetupScreenContent(
    uiState: SetupUiState,
    handleUiEvent: (SetupUiEvent) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    fun setup() {
        focusManager.clearFocus()
        handleUiEvent(SetupUiEvent.Setup)
    }

    Scaffold(
        topBar = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 20.dp, bottom = 40.dp)
            ) {
                AppName(
                    fontSize = 62.sp,
                    modifier = Modifier
                )

                Text(
                    text = stringResource(R.string.setupSetup),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.setup_create_your_password),
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.width(280.dp)
            )

            Spacer(Modifier.height(20.dp))

            PasswordField(
                value = uiState.password,
                onValueChange = { handleUiEvent(SetupUiEvent.PasswordChanged(it)) },
                label = stringResource(R.string.setup_enter_password),
                imeAction = ImeAction.Next,
            )

            AnimatedVisibility(
                visible = uiState.showConfirmPassword,
            ) {
                PasswordField(
                    value = uiState.confirmPassword,
                    onValueChange = { handleUiEvent(SetupUiEvent.ConfirmPasswordChanged(it)) },
                    label = stringResource(R.string.setup_confirm_password),
                    onDone = { setup() },
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            AnimatedVisibility(
                visible = uiState.passwordStrength != null,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(horizontal = 15.dp)
                ) {
                    Text(
                        text = stringResource(R.string.setup_password_strength_label),
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = stringResource(uiState.passwordStrength.labelRes()),
                        color = uiState.passwordStrength.color(),
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.passwordsMismatch,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text(
                    text = stringResource(R.string.setup_password_match_warning),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 20.dp)
                )
            }

            Button(
                onClick = { setup() },
                enabled = uiState.canSetup,
                modifier = Modifier
                    .width(200.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 20.dp)
            ) {
                if (uiState.loading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(stringResource(R.string.setup_button))
                }
            }
        }
    }
}

private fun PasswordStrength?.labelRes() = when (this) {
    PasswordStrength.VERY_WEAK -> R.string.setup_password_strength_very_weak
    PasswordStrength.WEAK -> R.string.setup_password_strength_weak
    PasswordStrength.MODERATE -> R.string.setup_password_strength_moderate
    PasswordStrength.STRONG -> R.string.setup_password_strength_strong
    PasswordStrength.VERY_STRONG -> R.string.setup_password_strength_very_strong
    null -> R.string.setup_password_strength_very_weak
}

@Composable
private fun PasswordStrength?.color() = when (this) {
    PasswordStrength.VERY_WEAK -> colorResource(R.color.darkRedStrong)
    PasswordStrength.WEAK -> colorResource(R.color.darkRed)
    PasswordStrength.MODERATE -> colorResource(R.color.darkYellow)
    PasswordStrength.STRONG -> colorResource(R.color.darkGreen)
    PasswordStrength.VERY_STRONG -> MaterialTheme.colorScheme.primary
    null -> MaterialTheme.colorScheme.outline
}

@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    AppTheme {
        SetupScreenContent(
            uiState = SetupUiState(
                password = "secret",
                confirmPassword = "secret",
                passwordStrength = PasswordStrength.STRONG,
                showConfirmPassword = true,
                canSetup = true,
            ),
            handleUiEvent = {},
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewMismatch() {
    AppTheme {
        SetupScreenContent(
            uiState = SetupUiState(
                password = "secret",
                confirmPassword = "secretx",
                passwordStrength = PasswordStrength.VERY_WEAK,
                showConfirmPassword = true,
                passwordsMismatch = true,
            ),
            handleUiEvent = {},
        )
    }
}
