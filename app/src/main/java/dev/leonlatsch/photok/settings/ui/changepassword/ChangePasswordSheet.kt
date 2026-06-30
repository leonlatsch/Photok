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

package dev.leonlatsch.photok.settings.ui.changepassword

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.PasswordUtils
import dev.leonlatsch.photok.ui.components.ConfirmationDialog
import dev.leonlatsch.photok.ui.components.DialogViewModelStoreOwner
import dev.leonlatsch.photok.ui.components.PasswordField
import dev.leonlatsch.photok.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordSheet(
    onDismissRequest: () -> Unit,
) {
    DialogViewModelStoreOwner {
        val viewModel: ChangePasswordViewModel = hiltViewModel()

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        SheetContent(
            uiState = uiState,
            handleUiEvent = viewModel::handleUiEvent,
            onDismissRequest = onDismissRequest,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetContent(
    uiState: ChangePasswordUiState,
    handleUiEvent: (ChangePasswordUiEvent) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.done) {
        if (uiState.done) {
            Toast.makeText(context, R.string.change_password_done, Toast.LENGTH_LONG).show()

            scope.launch {
                sheetState.hide()
            }.invokeOnCompletion {
                onDismissRequest()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.change_password_title),
                style = MaterialTheme.typography.headlineSmall,
            )

            if (uiState.recoveryPhraseWasUsed) {
                RecoveryPhraseUsedBanner()
            }

            AnimatedContent(
                targetState = uiState.step,
                transitionSpec = {
                    (slideInHorizontally { it } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it } + fadeOut())
                },
            ) { step ->
                when (step) {
                    ChangePasswordUiState.Step.CheckOld -> OldPasswordSection(
                        uiState = uiState,
                        handleUiEvent = handleUiEvent,
                    )
                    ChangePasswordUiState.Step.SetNew -> NewPasswordSection(
                        uiState = uiState,
                        handleUiEvent = handleUiEvent,
                    )
                }
            }
        }
    }

    ConfirmationDialog(
        show = uiState.showConfirmChangeDialog,
        onDismissRequest = { handleUiEvent(ChangePasswordUiEvent.UpdateShowConfirmationDialog(false)) },
        text = stringResource(R.string.common_are_you_sure),
        onConfirm = { handleUiEvent(ChangePasswordUiEvent.ConfirmChangePassword) }
    )
}

@Composable
private fun RecoveryPhraseUsedBanner() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check_circle),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = stringResource(R.string.change_password_recovery_phrase_used),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun OldPasswordSection(
    uiState: ChangePasswordUiState,
    handleUiEvent: (ChangePasswordUiEvent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PasswordField(
            value = uiState.oldPassword,
            onValueChange = { handleUiEvent(ChangePasswordUiEvent.OldPasswordChanged(it)) },
            label = stringResource(R.string.change_password_old_password),
            error = uiState.oldPasswordError,
            imeAction = ImeAction.Done,
            onDone = { handleUiEvent(ChangePasswordUiEvent.CheckOldPassword) },
        )

        Button(
            onClick = { handleUiEvent(ChangePasswordUiEvent.CheckOldPassword) },
            enabled = uiState.oldPassword.isNotEmpty() && !uiState.loading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.change_password_check))
        }
    }
}

@Composable
private fun NewPasswordSection(
    uiState: ChangePasswordUiState,
    handleUiEvent: (ChangePasswordUiEvent) -> Unit,
) {
    val newPasswordValid = PasswordUtils.validatePassword(uiState.newPassword)
    val passwordsMatch = PasswordUtils.passwordsNotEmptyAndEqual(uiState.newPassword, uiState.newPasswordConfirm)
    val canSubmit = PasswordUtils.validatePasswords(uiState.newPassword, uiState.newPasswordConfirm)
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(300.milliseconds)
        focusRequester.requestFocus()
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PasswordField(
            value = uiState.newPassword,
            onValueChange = { handleUiEvent(ChangePasswordUiEvent.NewPasswordChanged(it)) },
            label = stringResource(R.string.change_password_enter_new_password),
            imeAction = ImeAction.Next,
            modifier = Modifier.focusRequester(focusRequester),
        )

        AnimatedVisibility(visible = newPasswordValid) {
            PasswordField(
                value = uiState.newPasswordConfirm,
                onValueChange = { handleUiEvent(ChangePasswordUiEvent.NewPasswordConfirmChanged(it)) },
                label = stringResource(R.string.change_password_confirm_new_password),
                error = if (uiState.newPasswordConfirm.isNotEmpty() && !passwordsMatch) {
                    stringResource(R.string.setup_password_match_warning)
                } else null,
                imeAction = ImeAction.Done,
                onDone = { if (canSubmit) handleUiEvent(ChangePasswordUiEvent.UpdateShowConfirmationDialog(true)) },
            )
        }

        Button(
            onClick = { handleUiEvent(ChangePasswordUiEvent.UpdateShowConfirmationDialog(true)) },
            enabled = canSubmit && !uiState.loading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.change_password_button))
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewCheckOld() {
    AppTheme {
        SheetContent(
            uiState = ChangePasswordUiState(
                loading = false,
                recoveryPhraseWasUsed = false,
                step = ChangePasswordUiState.Step.CheckOld,
                oldPassword = "",
                newPassword = "",
                newPasswordConfirm = "",
                oldPasswordError = "",
                done = false,
            ),
            onDismissRequest = {},
            handleUiEvent = {}
        )
    }
}
