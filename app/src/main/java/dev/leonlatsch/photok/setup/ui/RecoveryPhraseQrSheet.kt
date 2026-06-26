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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.models.RecoveryPhrase
import dev.leonlatsch.photok.ui.components.AppName
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.uicomponnets.qr.QRCodeImage

@Composable
fun RecoveryPhraseQrSheet(
    phrase: RecoveryPhrase,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
) {
    val viewModel = hiltViewModel<RecoveryPhraseQrViewModel>()

    Content(
        phrase = phrase,
        handleUiEvent = viewModel::handleUiEvent,
        onDismiss = onDismiss,
        onSaved = onSaved,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    phrase: RecoveryPhrase,
    handleUiEvent: (RecoveryPhraseQrUiEvent) -> Unit,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
) {
    val context = LocalContext.current

    val saveQrLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/jpeg")) {
            it ?: return@rememberLauncherForActivityResult
            handleUiEvent(RecoveryPhraseQrUiEvent.SaveToFile(phrase, context, it))
            onSaved()
        }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = null,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp)
                .navigationBarsPadding()
        ) {
            AppName()

            Text(
                text = stringResource(R.string.recovery_phrase_label)
            )

            QRCodeImage(
                text = phrase.toMnemonicString(),
                foregroundColor = MaterialTheme.colorScheme.onSurfaceVariant,
                backgroundColor = BottomSheetDefaults.ContainerColor,
                modifier = Modifier
                    .size(200.dp)
            )

            Text(
                text = phrase.toMnemonicString().breakableAtDashes(),
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 30.dp)
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { saveQrLauncher.launch("photok-recovery-phrase.jpg") },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.recovery_phrase_qr_save_as_image))
            }
        }
    }
}

private fun String.breakableAtDashes() = replace("-", "-\u200B")

@PreviewLightDark
@Composable
private fun Preview() {
    AppTheme {
        Content(
            phrase = RecoveryPhrase(
                buildList {
                    repeat(12) { add("asd") }
                },
            ),
            handleUiEvent = {},
            onDismiss = {},
            onSaved = {},
        )
    }
}