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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
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
import dev.leonlatsch.photok.encryption.domain.crypto.Bip39MnemonicGenerator
import dev.leonlatsch.photok.encryption.domain.models.RecoveryPhrase
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.uicomponnets.qr.QrScannerView
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecoveryPhraseRestoreScreen(
    onUnlocked: () -> Unit,
    onBack: () -> Unit,
) {
    val viewModel: RecoveryPhraseRestoreViewModel = hiltViewModel()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.unlocked) {
        if (uiState.unlocked) {
            delay(3.seconds)
            onUnlocked()
        }
    }

    AppTheme {
        RecoveryPhraseRestoreContent(
            uiState = uiState,
            handleUiEvent = viewModel::handleUiEvent,
            onBack = onBack,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecoveryPhraseRestoreContent(
    uiState: RecoveryPhraseRestoreUiState,
    handleUiEvent: (RecoveryPhraseRestoreUiEvent) -> Unit,
    onBack: () -> Unit,
) {
    val clipboard = LocalClipboard.current

    val selectFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult

        handleUiEvent(RecoveryPhraseRestoreUiEvent.LoadFromFile(uri))
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = !uiState.unlocked,
                enter = EnterTransition.None,
                exit = slideOutVertically { it },
            ) {
                Button(
                    onClick = {
                        handleUiEvent(RecoveryPhraseRestoreUiEvent.Unlock(uiState.phrase))
                    },
                    enabled = uiState.phraseValid && !uiState.loading,
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
                        onClick = onBack,
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
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(horizontal = 24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.recovery_phrase_restore_enter_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(20.dp))

            AnimatedVisibility(uiState.selectedRestoreMethod == null) {
                Text(
                    text = stringResource(R.string.recovery_phrase_restore_choose_option),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                OptionButton(
                    text = stringResource(R.string.recovery_phrase_restore_option_type),
                    icon = R.drawable.ic_keyboard,
                    onClick = { handleUiEvent(RecoveryPhraseRestoreUiEvent.TypeByHand) },
                    restoreMethod = RecoveryPhraseRestoreUiState.RestoreMethod.TypeByHand,
                    selectedRestoreMethod = uiState.selectedRestoreMethod,
                )
                OptionButton(
                    text = stringResource(R.string.recovery_phrase_restore_option_open_file),
                    icon = R.drawable.ic_upload,
                    onClick = {
                        if (uiState.selectedRestoreMethod == null) {
                            selectFileLauncher.launch(arrayOf("text/plain"))
                        } else {
                            handleUiEvent(RecoveryPhraseRestoreUiEvent.ClearRestoreMethod)
                        }
                    },
                    restoreMethod = RecoveryPhraseRestoreUiState.RestoreMethod.LoadFromFile,
                    selectedRestoreMethod = uiState.selectedRestoreMethod,
                )
                OptionButton(
                    text = stringResource(R.string.recovery_phrase_restore_option_scan_qr),
                    icon = R.drawable.ic_qr_code,
                    onClick = { handleUiEvent(RecoveryPhraseRestoreUiEvent.ScanQrCode) },
                    restoreMethod = RecoveryPhraseRestoreUiState.RestoreMethod.ScanQrCode,
                    selectedRestoreMethod = uiState.selectedRestoreMethod,
                )
                OptionButton(
                    text = stringResource(R.string.recovery_phrase_restore_option_paste),
                    icon = R.drawable.ic_paste,
                    onClick = {
                        if (uiState.selectedRestoreMethod == null) {
                            handleUiEvent(
                                RecoveryPhraseRestoreUiEvent.PasteFromClipboard(
                                    clipboard
                                )
                            )
                        } else {
                            handleUiEvent(RecoveryPhraseRestoreUiEvent.ClearRestoreMethod)
                        }
                    },
                    restoreMethod = RecoveryPhraseRestoreUiState.RestoreMethod.PasteFromClipboard,
                    selectedRestoreMethod = uiState.selectedRestoreMethod,
                )
            }


            val focusRequester = remember { FocusRequester() }
            val focusManager = LocalFocusManager.current

            LaunchedEffect(uiState.selectedRestoreMethod) {
                if (uiState.selectedRestoreMethod == RecoveryPhraseRestoreUiState.RestoreMethod.TypeByHand) {
                    delay(800.milliseconds)
                    focusRequester.requestFocus()
                }

                if (uiState.selectedRestoreMethod == null) {
                    focusManager.clearFocus()
                }
            }

            AnimatedVisibility(
                visible = uiState.restoreSupportingText != null,
            ) {
                Text(
                    text = uiState.restoreSupportingText.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 20.dp)
                )
            }

            AnimatedContent(
                uiState.selectedRestoreMethod,
                transitionSpec = {
                    fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                },
            ) {
                when (it) {
                    RecoveryPhraseRestoreUiState.RestoreMethod.TypeByHand -> {
                        OutlinedTextField(
                            value = uiState.phrase.toMnemonicString(),
                            onValueChange = {
                                val new = it
                                    .replace(" ", "-")
                                    .replace("\n", "-")
                                    .replace("--", "-")

                                handleUiEvent(
                                    RecoveryPhraseRestoreUiEvent.UpdatePhrase(
                                        RecoveryPhrase.from(new)
                                    )
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.None,
                                autoCorrectEnabled = false,
                                imeAction = ImeAction.Done,
                                keyboardType = KeyboardType.Ascii,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            maxLines = 4,
                            shape = RoundedCornerShape(24.dp),
                            label = {
                                Text(stringResource(R.string.recovery_phrase_restore_input_label))
                            },
                            placeholder = {
                                Text(stringResource(R.string.recovery_phrase_restore_input_placeholder))
                            },
                            modifier = Modifier
                                .height(200.dp)
                                .focusRequester(focusRequester)
                                .padding(vertical = 20.dp)
                        )
                    }

                    RecoveryPhraseRestoreUiState.RestoreMethod.ScanQrCode -> {
                        AnimatedVisibility(
                            visible = uiState.phrase.words.isEmpty(),
                        ) {
                            QrScannerView(
                                onQrCodeDecoded = { qrCodeContent ->
                                    handleUiEvent(
                                        RecoveryPhraseRestoreUiEvent.QrScanned(
                                            qrCodeContent
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .height(200.dp)
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp)
                            )
                        }
                    }

                    RecoveryPhraseRestoreUiState.RestoreMethod.PasteFromClipboard,
                    RecoveryPhraseRestoreUiState.RestoreMethod.LoadFromFile,
                    null -> Box(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            AnimatedVisibility(
                uiState.phrase.words.isNotEmpty() && uiState.selectedRestoreMethod != RecoveryPhraseRestoreUiState.RestoreMethod.TypeByHand,
            ) {
                RecoveryPhraseFlowRow(
                    phrase = uiState.phrase,
                    animated = true,
                )
            }

            AnimatedVisibility(uiState.error != null) {
                Text(
                    text = uiState.error.orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            AnimatedVisibility(uiState.loading) {
                Crossfade(uiState.unlocked) { unlocked ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (unlocked) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            Text(
                                text = stringResource(R.string.recovery_phrase_restore_unlocked),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )

                            Text(
                                text = stringResource(R.string.recovery_phrase_restore_unlocking),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
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

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onClick,
                modifier = modifier,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = color,
                ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                    )
                    Text(
                        text = text,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    AnimatedVisibility(selected) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
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
                phraseValid = false,
                loading = false,
            ),
            handleUiEvent = {},
            onBack = {},
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
                phraseValid = false,
                loading = false,
                selectedRestoreMethod = RecoveryPhraseRestoreUiState.RestoreMethod.TypeByHand
            ),
            handleUiEvent = {},
            onBack = {},
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewQr() {
    AppTheme {
        RecoveryPhraseRestoreContent(
            uiState = RecoveryPhraseRestoreUiState(
                phrase = RecoveryPhrase(),
                phraseValid = false,
                loading = false,
                selectedRestoreMethod = RecoveryPhraseRestoreUiState.RestoreMethod.ScanQrCode,
            ),
            handleUiEvent = {},
            onBack = {},
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewFromFile() {
    val context = LocalContext.current

    AppTheme {
        RecoveryPhraseRestoreContent(
            uiState = RecoveryPhraseRestoreUiState(
                phrase = RecoveryPhrase(
                    Bip39MnemonicGenerator(context).generate()
                ),
                phraseValid = true,
                loading = false,
                selectedRestoreMethod = RecoveryPhraseRestoreUiState.RestoreMethod.LoadFromFile,
                restoreSupportingText = "Loaded from file"
            ),
            handleUiEvent = {},
            onBack = {},
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewTypePasted() {
    val context = LocalContext.current

    AppTheme {
        RecoveryPhraseRestoreContent(
            uiState = RecoveryPhraseRestoreUiState(
                phrase = RecoveryPhrase(
                    Bip39MnemonicGenerator(context).generate()
                ),
                phraseValid = true,
                loading = false,
                selectedRestoreMethod = RecoveryPhraseRestoreUiState.RestoreMethod.PasteFromClipboard,
                restoreSupportingText = "Pasted from clipboard"
            ),
            handleUiEvent = {},
            onBack = {},
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewUnlocked() {
    val context = LocalContext.current

    AppTheme {
        RecoveryPhraseRestoreContent(

            uiState = RecoveryPhraseRestoreUiState(
                phrase = RecoveryPhrase(
                    Bip39MnemonicGenerator(context).generate()
                ),
                phraseValid = true,
                loading = true,
                unlocked = true,
                selectedRestoreMethod = RecoveryPhraseRestoreUiState.RestoreMethod.PasteFromClipboard,
                restoreSupportingText = "Pasted from clipboard"
            ),
            handleUiEvent = {},
            onBack = {},
        )
    }
}
