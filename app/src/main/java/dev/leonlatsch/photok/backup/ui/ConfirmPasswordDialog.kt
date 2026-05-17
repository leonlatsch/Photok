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

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.components.DialogViewModelStoreOwner
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
                    Text(
                        text = subtitle,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    var passwordVisible by rememberSaveable { mutableStateOf(false) }

                    OutlinedTextField(
                        value = uiState.password,
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        onValueChange = {
                            handleUiEvent(ConfirmPasswordUiEvent.PasswordValueChange(it))
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                handleUiEvent(ConfirmPasswordUiEvent.ConfirmPassword)
                            }
                        ),
                        isError = uiState.error != null,
                        supportingText = if (uiState.error == null) {
                            null
                        } else {
                            {
                                Text(uiState.error)
                            }
                        },
                        maxLines = 1,
                        label = {
                            Text(
                                text = stringResource(R.string.common_password)
                            )
                        },
                        suffix = {
                            val icon = if (passwordVisible) {
                                R.drawable.ic_eye_closed
                            } else {
                                R.drawable.ic_eye
                            }

                            Crossfade(icon) { icon ->
                                Icon(
                                    painter = painterResource(icon),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .size(24.dp)
                                        .clickable(role = Role.Button) {
                                            passwordVisible = !passwordVisible
                                        }
                                )
                            }

                        }
                    )
                }
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
