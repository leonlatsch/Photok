/*
 *   Copyright 2020-2026 Leon Latsch
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

package dev.leonlatsch.photok.backup.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.components.DialogViewModelStoreOwner
import dev.leonlatsch.photok.ui.components.PasswordField
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun ConfirmPasswordDialog(
    visible: Boolean,
    subtitle: String? = null,
    onSuccess: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    if (visible) {
        DialogViewModelStoreOwner {
            val viewModel = hiltViewModel<ConfirmpasswordViewModel>()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.success) {
                if (uiState.success) {
                    onSuccess()
                    onDismissRequest()
                }
            }

            Content(
                uiState = uiState,
                subtitle = subtitle,
                handleUiEvent = viewModel::handleUiEvent,
                onDismissRequest = onDismissRequest
            )
        }
    }
}

@Composable
private fun Content(
    uiState: ConfirmPasswordUiState,
    subtitle: String? = null,
    handleUiEvent: (ConfirmPasswordUiEvent) -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(
                onClick = {
                    handleUiEvent(ConfirmPasswordUiEvent.ConfirmPassword)
                },
                enabled = uiState.password.isNotEmpty() && !uiState.loading,
            ) {
                Text(stringResource(R.string.common_ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                enabled = !uiState.loading,
            ) {
                Text(stringResource(R.string.common_cancel))
            }
        },
        title = {
            Text(
                text = stringResource(R.string.setup_confirm_password)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (subtitle != null) {
                    Text(text = subtitle)
                }

                PasswordField(
                    value = uiState.password,
                    onValueChange = { handleUiEvent(ConfirmPasswordUiEvent.PasswordValueChange(it)) },
                    label = stringResource(R.string.common_password),
                    error = uiState.error,
                    imeAction = ImeAction.Done,
                    onDone = { handleUiEvent(ConfirmPasswordUiEvent.ConfirmPassword) },
                )
            }
        }
    )
}

@PreviewLightDark
@Composable
private fun Preview() {
    AppTheme {
        Content(
            uiState = ConfirmPasswordUiState(password = "asd"),
            subtitle = "Subtitle",
            handleUiEvent = {},
            onDismissRequest = {},
        )
    }
}
