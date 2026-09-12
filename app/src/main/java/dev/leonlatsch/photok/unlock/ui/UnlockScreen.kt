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

package dev.leonlatsch.photok.unlock.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.LocalFragment
import dev.leonlatsch.photok.ui.components.PasswordField
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.uicomponents.AppName
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun UnlockScreen(viewModel: UnlockViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AppTheme {
        UnlockScreenContent(
            uiState = uiState,
            handleUiEvent = viewModel::handleUiEvent,
        )
    }
}

@Composable
private fun UnlockScreenContent(
    uiState: UnlockUiState,
    handleUiEvent: (UnlockUiEvent) -> Unit,
) {
    val fragment = LocalFragment.current
    val focusManager = LocalFocusManager.current

    fun unlockWithPassword() {
        focusManager.clearFocus()
        handleUiEvent(UnlockUiEvent.UnlockWithPassword)
    }

    // Offer biometrics right away, but only once per screen so a canceled prompt stays canceled.
    var biometricPromptShown by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(uiState.biometricAvailable, uiState.lockedUntil) {
        if (biometricPromptShown || !uiState.biometricAvailable || uiState.lockedUntil != null) {
            return@LaunchedEffect
        }
        fragment ?: return@LaunchedEffect

        biometricPromptShown = true
        delay(0.5.seconds)
        handleUiEvent(UnlockUiEvent.UnlockWithBiometric(fragment))
    }

    Scaffold(
        topBar = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                AppName(
                    fontSize = 62.sp,
                    modifier = Modifier
                        .padding(top = 20.dp, bottom = 40.dp)
                )
            }
        },
        bottomBar = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                AnimatedVisibility(uiState.biometricAvailable) {
                    TextButton(
                        onClick = {
                            fragment ?: return@TextButton
                            handleUiEvent(UnlockUiEvent.UnlockWithBiometric(fragment))
                        },
                    ) {
                        Text(stringResource(R.string.biometric_unlock_hint_button))
                    }
                }
            }
        }
    ) { contentPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = stringResource(R.string.unlock_title),
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.width(280.dp)
                )

                Spacer(Modifier.height(20.dp))

                PasswordField(
                    value = uiState.password,
                    onValueChange = { handleUiEvent(UnlockUiEvent.PasswordChanged(it)) },
                    label = stringResource(R.string.unlock_enter_password),
                    error = stringResource(R.string.unlock_wrong_password).takeIf { uiState.wrongPassword },
                    onDone = { unlockWithPassword() },
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { unlockWithPassword() },
                    enabled = !uiState.loading,
                    modifier = Modifier
                        .width(200.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    if (uiState.loading) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(stringResource(R.string.unlock_button))
                    }
                }

                AnimatedVisibility(
                    visible = uiState.recoveryPhraseAvailable,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    TextButton(
                        onClick = { handleUiEvent(UnlockUiEvent.ForgotPassword) },
                    ) {
                        Text(stringResource(R.string.recovery_phrase_forgot_password))
                    }
                }
            }

            if (uiState.lockedUntil != null) {
                LockoutContent(lockedUntil = uiState.lockedUntil)
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    AppTheme {
        UnlockScreenContent(
            uiState = UnlockUiState(
                password = "secret",
                biometricAvailable = true,
                recoveryPhraseAvailable = true,
            ),
            handleUiEvent = {},
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewWrongPassword() {
    AppTheme {
        UnlockScreenContent(
            uiState = UnlockUiState(password = "secret", wrongPassword = true),
            handleUiEvent = {},
        )
    }
}
