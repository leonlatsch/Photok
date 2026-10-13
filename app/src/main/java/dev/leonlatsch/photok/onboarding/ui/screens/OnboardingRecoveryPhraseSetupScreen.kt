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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.crypto.Bip39WordCount
import dev.leonlatsch.photok.encryption.domain.models.RecoveryPhrase
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingNexButtonBar
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingPageSubtitle
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingPageTitle
import dev.leonlatsch.photok.recovery.ui.RecoveryPhraseFlowRow
import dev.leonlatsch.photok.recovery.ui.RecoveryPhraseQrSheet
import dev.leonlatsch.photok.recovery.ui.RecoveryPhraseUiEvent
import dev.leonlatsch.photok.recovery.ui.RecoveryPhraseUiState
import dev.leonlatsch.photok.recovery.ui.RecoveryPhraseViewModel
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.theme.Dimens

@Composable
fun OnboardingRecoveryPhraseSetupScreen(onFinish: () -> Unit) {
    val viewModel: RecoveryPhraseViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingRecoveryPhraseSetupContent(
        uiState = uiState,
        handleUiEvent = viewModel::handleUiEvent,
        onFinish = onFinish,
    )
}

@Composable
private fun OnboardingRecoveryPhraseSetupContent(
    uiState: RecoveryPhraseUiState,
    handleUiEvent: (RecoveryPhraseUiEvent) -> Unit,
    onFinish: () -> Unit,
) {
    val phrase = uiState.phrase
    if (uiState.inputs.qrSheetVisible && phrase != null) {
        RecoveryPhraseQrSheet(
            phrase = phrase,
            onDismiss = { handleUiEvent(RecoveryPhraseUiEvent.DismissQrSheet) },
            onSaved = { handleUiEvent(RecoveryPhraseUiEvent.MarkPhraseSaved) },
        )
    }

    Scaffold(
        bottomBar = {
            Column {
                RecoveryPhraseActions(
                    phrase = phrase,
                    handleUiEvent = handleUiEvent,
                )

                OnboardingNexButtonBar(
                    text = stringResource(R.string.recovery_phrase_confirm),
                    enabled = uiState.inputs.phraseWasSaved,
                    loading = uiState.inputs.loading,
                    onNext = onFinish,
                )
            }
        }
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
            modifier = Modifier
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.spacingLarge)
        ) {
            OnboardingPageTitle(
                text = stringResource(R.string.recovery_phrase_title)
            )

            OnboardingPageSubtitle(
                text = stringResource(R.string.recovery_phrase_description)
            )

            RecoveryPhraseFlowRow(
                phrase = phrase,
                animated = true,
            )

            RecoveryPhraseWordCountSelection(
                wordCount = uiState.inputs.wordCount,
                handleUiEvent = handleUiEvent,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

@Composable
private fun RecoveryPhraseActions(
    phrase: RecoveryPhrase?,
    handleUiEvent: (RecoveryPhraseUiEvent) -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current

    val selectFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) {
        it ?: return@rememberLauncherForActivityResult
        phrase ?: return@rememberLauncherForActivityResult

        handleUiEvent(RecoveryPhraseUiEvent.SaveToFile(context, it, phrase))
    }

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(
            onClick = { selectFileLauncher.launch("photok-recovery-phrase.txt") },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_download),
                contentDescription = null,
            )
        }
        IconButton(
            onClick = {
                phrase ?: return@IconButton
                handleUiEvent(RecoveryPhraseUiEvent.Share(context, phrase))
            },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_share),
                contentDescription = null,
            )
        }
        IconButton(
            onClick = {
                phrase ?: return@IconButton
                handleUiEvent(RecoveryPhraseUiEvent.ShowQrCode)
            },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_qr_code),
                contentDescription = null,
            )
        }
        IconButton(
            onClick = {
                phrase ?: return@IconButton
                handleUiEvent(RecoveryPhraseUiEvent.CopyToClipboard(clipboard, phrase))
            },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_content_copy),
                contentDescription = null,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecoveryPhraseWordCountSelection(
    wordCount: Bip39WordCount,
    handleUiEvent: (RecoveryPhraseUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalHapticFeedback.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.recovery_phrase_select_word_count)
        )

        SingleChoiceSegmentedButtonRow {
            SegmentedButton(
                selected = wordCount == Bip39WordCount.Twelve,
                onClick = {
                    handleUiEvent(RecoveryPhraseUiEvent.UpdateWordCount(Bip39WordCount.Twelve))
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                },
                shape = RoundedCornerShape(24.dp, 6.dp, 6.dp, 24.dp),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    activeBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
            ) {
                Text(Bip39WordCount.Twelve.words.toString())
            }
            Spacer(Modifier.width(6.dp))
            SegmentedButton(
                selected = wordCount == Bip39WordCount.TwentyFour,
                onClick = {
                    handleUiEvent(RecoveryPhraseUiEvent.UpdateWordCount(Bip39WordCount.TwentyFour))
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                },
                shape = RoundedCornerShape(6.dp, 24.dp, 24.dp, 6.dp),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    activeBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
            ) {
                Text(Bip39WordCount.TwentyFour.words.toString())
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun OnboardingRecoveryPhraseSetupScreenPreview() {
    AppTheme {
        OnboardingRecoveryPhraseSetupContent(
            uiState = RecoveryPhraseUiState(
                phrase = RecoveryPhrase(
                    listOf(
                        "this",
                        "can",
                        "be",
                        "any",
                        "words",
                        "generated",
                        "by",
                        "photok",
                        "and",
                        "its",
                        "always",
                        "twelve"
                    ),
                ),
            ),
            handleUiEvent = {},
            onFinish = {},
        )
    }
}
