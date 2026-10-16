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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.navigation.LocalNavigator
import dev.leonlatsch.photok.onboarding.ui.OnboardingRoute
import dev.leonlatsch.photok.onboarding.ui.SetupNavigationEvent
import dev.leonlatsch.photok.onboarding.ui.SetupUiEvent
import dev.leonlatsch.photok.onboarding.ui.SetupUiState
import dev.leonlatsch.photok.onboarding.ui.SetupViewModel
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingNexButtonBar
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingPageSubtitle
import dev.leonlatsch.photok.other.extensions.hideKeyboard
import dev.leonlatsch.photok.ui.ObserveAsEvents
import dev.leonlatsch.photok.ui.components.PasswordField
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.theme.Dimens
import dev.leonlatsch.photok.uicomponnets.Dialogs

@Composable
fun OnboardingSetupPasswordScreen(
    viewModel: SetupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val navigator = LocalNavigator.current
    val activity = LocalActivity.current
    val context = LocalContext.current
    val resources = LocalResources.current

    ObserveAsEvents(viewModel.navigationEvents) { event ->
        when (event) {
            SetupNavigationEvent.ShowRecoveryPhraseSetup -> {
                activity?.hideKeyboard()
                navigator.navigate(OnboardingRoute.BiometricUnlock)
            }

            SetupNavigationEvent.ShowError -> {
                Dialogs.showLongToast(context, resources.getString(R.string.common_error))
            }
        }
    }

    OnboardingSetupPasswordContent(
        uiState = uiState,
        handleUiEvent = viewModel::handleUiEvent,
    )
}

@Composable
private fun OnboardingSetupPasswordContent(
    uiState: SetupUiState,
    handleUiEvent: (SetupUiEvent) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        bottomBar = {
            OnboardingNexButtonBar(
                enabled = uiState.canSetup,
                loading = uiState.loading,
            ) {
                focusManager.clearFocus()
                handleUiEvent(SetupUiEvent.Setup)
            }
        }
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(Dimens.spacingLarge)
        ) {
            OnboardingPageSubtitle(
                text = "Remember your password and keep it safe. For your security, we don’t store it, so it cannot be reset."
            )

            Spacer(Modifier.height(Dimens.spacingXSmall))

            PasswordField(
                value = uiState.password,
                onValueChange = { handleUiEvent(SetupUiEvent.PasswordChanged(it)) },
                label = "Password",
                imeAction = ImeAction.Next,
            )

            AnimatedVisibility(
                visible = uiState.showConfirmPassword,
            ) {
                PasswordField(
                    value = uiState.confirmPassword,
                    onValueChange = { handleUiEvent(SetupUiEvent.ConfirmPasswordChanged(it)) },
                    label = "Confirm password",
                    error = stringResource(R.string.setup_password_match_warning).takeIf { uiState.passwordsMismatch },
                    onDone = { focusManager.clearFocus() },
                    modifier = Modifier.imePadding()
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun OnboardingSetupPasswordScreenPreview() {
    AppTheme {
        OnboardingSetupPasswordContent(
            uiState = SetupUiState(
                password = "secret",
                confirmPassword = "secret",
                showConfirmPassword = true,
                canSetup = true,
            ),
            handleUiEvent = {},
        )
    }
}
