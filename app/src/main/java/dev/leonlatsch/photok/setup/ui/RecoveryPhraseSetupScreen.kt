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

package dev.leonlatsch.photok.setup.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.crypto.Bip39WordCount
import dev.leonlatsch.photok.encryption.domain.models.RecoveryPhrase
import dev.leonlatsch.photok.encryption.ui.RecoveryPhraseFlowRow
import dev.leonlatsch.photok.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecoveryPhraseSetupScreen(
    onContinue: () -> Unit,
) {
    // TODO: Can choose word count and download as txt, print, and copy to clipboard
    // TODO: Only allow continue if saved in any way
    // TODO: Animate tap on word
    // TODO: Handle small device (scroll?)
    val viewModel: RecoveryPhraseSetupViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        handleUiEvent = viewModel::handleUiEvent,
        onContinue = onContinue,
    )
}

@Composable
private fun Content(
    uiState: RecoveryPhraseSetupUiState,
    handleUiEvent: (RecoveryPhraseSetupUiEvent) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        bottomBar = {
            Button(
                onClick = onContinue,
                modifier = modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.recovery_phrase_confirm))
            }
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.recovery_phrase_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.recovery_phrase_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            RecoveryPhraseFlowRow(
                phrase = uiState.phrase,
                animated = true,
            )

            Text(
                text = "Select Phrase Word Count"
            )

            SingleChoiceSegmentedButtonRow {
                SegmentedButton(
                    selected = uiState.inputs.wordCount == Bip39WordCount.Twelve,
                    onClick = {
                        handleUiEvent(RecoveryPhraseSetupUiEvent.UpdateWordCount(Bip39WordCount.Twelve))
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
                    selected = uiState.inputs.wordCount == Bip39WordCount.TwentyFour,
                    onClick = {
                        handleUiEvent(RecoveryPhraseSetupUiEvent.UpdateWordCount(Bip39WordCount.TwentyFour))
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
}

@PreviewLightDark
@Composable
private fun Preview() {
    AppTheme {
        Content(
            uiState = RecoveryPhraseSetupUiState(
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
            onContinue = {},
        )
    }
}