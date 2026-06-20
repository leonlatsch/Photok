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

package dev.leonlatsch.photok.encryption.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.models.RecoveryPhrase
import dev.leonlatsch.photok.ui.theme.AppTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecoveryPhraseRestoreScreen(
    onRestored: () -> Unit,
) {
    val viewModel: RecoveryPhraseRestoreViewModel = hiltViewModel()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.unlocked) {
        if (uiState.unlocked) {
            delay(3000)
            onRestored()
        }
    }

    AppTheme {
        RecoveryPhraseRestoreContent(
            uiState = uiState,
            handleUiEvent = viewModel::handleUiEvent,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecoveryPhraseRestoreContent(
    uiState: RecoveryPhraseRestoreUiState,
    handleUiEvent: (RecoveryPhraseRestoreUiEvent) -> Unit,
) {
    Scaffold(
        bottomBar = {
            AnimatedVisibility(!uiState.unlocked) {
                Button(
                    onClick = {
                        handleUiEvent(RecoveryPhraseRestoreUiEvent.Restore(uiState.phrase))
                    },
                    enabled = uiState.validInput,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .navigationBarsPadding()
                        .fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.recovery_phrase_restore_button))
                }
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.recovery_phrase_forgot_password)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = stringResource(R.string.common_cancel)
                        )
                    }
                }
            )
        },
    ) { contentPadding ->
        AnimatedVisibility(
            visible = !uiState.unlocked
        ) {
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(horizontal = 24.dp)
                    .fillMaxSize()
                ,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Enter Recovery Phrase",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(20.dp))

                AnimatedVisibility(uiState.selectedRestoreMethod == null) {
                    Text(
                        text = "Enter your recovery phrase to restore your vault. Choose one option for input.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    OptionButton(
                        text = "Type by hand",
                        icon = R.drawable.ic_keyboard,
                        onClick = { handleUiEvent(RecoveryPhraseRestoreUiEvent.TypeByHand) },
                        restoreMethod = RecoveryPhraseRestoreUiState.RestoreMethod.TypeByHand,
                        selectedRestoreMethod = uiState.selectedRestoreMethod,
                    )
                    OptionButton(
                        text = "Open file",
                        icon = R.drawable.ic_upload,
                        onClick = { },
                        restoreMethod = RecoveryPhraseRestoreUiState.RestoreMethod.LoadFromFile,
                        selectedRestoreMethod = uiState.selectedRestoreMethod,
                    )
                    OptionButton(
                        text = "Scan QR Code",
                        icon = R.drawable.ic_qr_code,
                        onClick = { },
                        restoreMethod = RecoveryPhraseRestoreUiState.RestoreMethod.ScanQrCode,
                        selectedRestoreMethod = uiState.selectedRestoreMethod,
                    )
                    OptionButton(
                        text = "Paste from Clipboard",
                        icon = R.drawable.ic_paste,
                        onClick = { },
                        restoreMethod = RecoveryPhraseRestoreUiState.RestoreMethod.PasteFromClipboard,
                        selectedRestoreMethod = uiState.selectedRestoreMethod,
                    )
                }

                val focusRequester = remember { FocusRequester() }
                val focusManager = LocalFocusManager.current

                LaunchedEffect(uiState.selectedRestoreMethod) {
                    if (uiState.selectedRestoreMethod == RecoveryPhraseRestoreUiState.RestoreMethod.TypeByHand) {
                        delay(500)
                        focusRequester.requestFocus()
                    }
                }

                AnimatedContent(
                    uiState.selectedRestoreMethod,
                    transitionSpec = {
                        expandVertically { it }.togetherWith(shrinkVertically { 0 })
                    },
                    modifier = Modifier.padding(top = 40.dp)
                ) {
                    when (it) {
                        RecoveryPhraseRestoreUiState.RestoreMethod.TypeByHand -> {
                            var text by rememberSaveable { mutableStateOf("") }
                            OutlinedTextField(
                                value = text,
                                onValueChange = {
                                    text = it
                                        .replace(" ", "-")
                                        .replace("\n", "-")
                                        .replace("--", "-")
                                },
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.None,
                                    autoCorrectEnabled = false,
                                    imeAction = ImeAction.Done,
                                    keyboardType = KeyboardType.Ascii,
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        // TODO
                                    }
                                ),
                                maxLines = 4,
                                shape = RoundedCornerShape(24.dp),
                                label = {
                                    Text("Recovery Phrase")
                                },
                                placeholder = {
                                    Text("Type your place word by word")
                                },
                                modifier = Modifier
                                    .height(200.dp)
                                    .focusRequester(focusRequester)
                            )
                        }

                        RecoveryPhraseRestoreUiState.RestoreMethod.PasteFromClipboard -> Unit
                        RecoveryPhraseRestoreUiState.RestoreMethod.ScanQrCode -> Unit
                        RecoveryPhraseRestoreUiState.RestoreMethod.LoadFromFile -> Unit
                        null -> Unit
                    }
                }

                AnimatedVisibility(uiState.error != null) {
                    Text(
                        text = "Could not restore vault from recovery phrase.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.OptionButton(
    text: String,
    icon: Int,
    onClick: () -> Unit,
    selectedRestoreMethod: RecoveryPhraseRestoreUiState.RestoreMethod?,
    restoreMethod: RecoveryPhraseRestoreUiState.RestoreMethod,
    modifier: Modifier = Modifier,
) {
    val selected = selectedRestoreMethod == restoreMethod

    AnimatedVisibility(
        visible = selectedRestoreMethod == null || selected,
    ) {

        Column {
            val color by animateColorAsState(
                if (selected) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )

            OutlinedButton(
                onClick = onClick,
                modifier = modifier,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = color,
                ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                    )
                    Text(
                        text = text
                    )
                    AnimatedVisibility(selected) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    AppTheme {
        RecoveryPhraseRestoreContent(
            uiState = RecoveryPhraseRestoreUiState(
                phrase = RecoveryPhrase(),
                validInput = false,
                loading = false,
            ),
            handleUiEvent = {},
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewTypeByHand() {
    AppTheme {
        RecoveryPhraseRestoreContent(
            uiState = RecoveryPhraseRestoreUiState(
                phrase = RecoveryPhrase(),
                validInput = false,
                loading = false,
                selectedRestoreMethod = RecoveryPhraseRestoreUiState.RestoreMethod.TypeByHand
            ),
            handleUiEvent = {},
        )
    }
}
