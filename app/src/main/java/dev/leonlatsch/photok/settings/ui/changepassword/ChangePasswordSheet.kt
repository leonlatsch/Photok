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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import dev.leonlatsch.photok.ui.components.DialogViewModelStoreOwner
import dev.leonlatsch.photok.ui.components.PasswordField
import dev.leonlatsch.photok.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordSheet(
    onDismissRequest: () -> Unit,
) {
    DialogViewModelStoreOwner {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val viewModel: ChangePasswordViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val context = LocalContext.current

        LaunchedEffect(uiState.done) {
            if (uiState.done) {
                Toast.makeText(context, R.string.change_password_done, Toast.LENGTH_LONG).show()
                sheetState.hide()
                onDismissRequest()
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
                    label = "ChangePasswordStep",
                ) { step ->
                    when (step) {
                        ChangePasswordStep.CheckOld -> OldPasswordSection(
                            uiState = uiState,
                            onEvent = viewModel::handleUiEvent,
                        )
                        ChangePasswordStep.SetNew -> NewPasswordSection(
                            uiState = uiState,
                            onEvent = viewModel::handleUiEvent,
                        )
                    }
                }
            }
        }
    }
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
    onEvent: (ChangePasswordUiEvent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PasswordField(
            value = uiState.oldPassword,
            onValueChange = { onEvent(ChangePasswordUiEvent.OldPasswordChanged(it)) },
            label = stringResource(R.string.change_password_old_password),
            error = uiState.oldPasswordError,
            imeAction = ImeAction.Done,
            onDone = { onEvent(ChangePasswordUiEvent.CheckOldPassword) },
        )

        Button(
            onClick = { onEvent(ChangePasswordUiEvent.CheckOldPassword) },
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
    onEvent: (ChangePasswordUiEvent) -> Unit,
) {
    val newPasswordValid = PasswordUtils.validatePassword(uiState.newPassword)
    val passwordsMatch = PasswordUtils.passwordsNotEmptyAndEqual(uiState.newPassword, uiState.newPasswordConfirm)
    val canSubmit = PasswordUtils.validatePasswords(uiState.newPassword, uiState.newPasswordConfirm)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PasswordField(
            value = uiState.newPassword,
            onValueChange = { onEvent(ChangePasswordUiEvent.NewPasswordChanged(it)) },
            label = stringResource(R.string.change_password_enter_new_password),
            imeAction = if (newPasswordValid) ImeAction.Next else ImeAction.Done,
        )

        AnimatedVisibility(visible = newPasswordValid) {
            PasswordField(
                value = uiState.newPasswordConfirm,
                onValueChange = { onEvent(ChangePasswordUiEvent.NewPasswordConfirmChanged(it)) },
                label = stringResource(R.string.change_password_confirm_new_password),
                error = if (uiState.newPasswordConfirm.isNotEmpty() && !passwordsMatch) {
                    stringResource(R.string.setup_password_match_warning)
                } else null,
                imeAction = ImeAction.Done,
                onDone = { if (canSubmit) onEvent(ChangePasswordUiEvent.ChangePassword) },
            )
        }

        Button(
            onClick = { onEvent(ChangePasswordUiEvent.ChangePassword) },
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
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.change_password_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
                OldPasswordSection(
                    uiState = ChangePasswordUiState(),
                    onEvent = {},
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewSetNew() {
    AppTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.change_password_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
                RecoveryPhraseUsedBanner()
                NewPasswordSection(
                    uiState = ChangePasswordUiState(
                        step = ChangePasswordStep.SetNew,
                        recoveryPhraseWasUsed = true,
                        newPassword = "password",
                    ),
                    onEvent = {},
                )
            }
        }
    }
}
