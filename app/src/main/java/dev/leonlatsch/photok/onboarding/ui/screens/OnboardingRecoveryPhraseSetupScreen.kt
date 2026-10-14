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

import android.app.Activity
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.crypto.Bip39WordCount
import dev.leonlatsch.photok.encryption.domain.models.RecoveryPhrase
import dev.leonlatsch.photok.navigation.LocalNavigator
import dev.leonlatsch.photok.onboarding.ui.OnboardingRoute
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingNexButtonBar
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingPageSubtitle
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingPageTitle
import dev.leonlatsch.photok.onboarding.ui.recoveryphrase.OnboardingRecoveryPhraseNavigationEvent
import dev.leonlatsch.photok.onboarding.ui.recoveryphrase.OnboardingRecoveryPhraseUiEvent
import dev.leonlatsch.photok.onboarding.ui.recoveryphrase.OnboardingRecoveryPhraseUiState
import dev.leonlatsch.photok.onboarding.ui.recoveryphrase.OnboardingRecoveryPhraseViewModel
import dev.leonlatsch.photok.recovery.ui.breakableAtDashes
import dev.leonlatsch.photok.recovery.ui.recoveryPhraseChip
import dev.leonlatsch.photok.ui.ObserveAsEvents
import dev.leonlatsch.photok.ui.components.RoundedDropdownMenu
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.theme.Dimens
import dev.leonlatsch.photok.uicomponnets.qr.QRCodeImage

@ChecksSdkIntAtLeast(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun canCaptureScreenshots(): Boolean {
   return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
}

@Composable
fun OnboardingRecoveryPhraseSetupScreen() {
    val viewModel: OnboardingRecoveryPhraseViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current

    ObserveAsEvents(viewModel.navigationEvents) { event ->
        when (event) {
            OnboardingRecoveryPhraseNavigationEvent.Continue -> navigator.navigate(OnboardingRoute.AddFirstPhotos)
        }
    }

    LifecycleResumeEffect(Unit) {
        if (canCaptureScreenshots()) {
            viewModel.handleUiEvent(OnboardingRecoveryPhraseUiEvent.AllowScreenshots)
        }
        onPauseOrDispose { viewModel.handleUiEvent(OnboardingRecoveryPhraseUiEvent.RevokeScreenshots) }
    }

    val activity = LocalActivity.current
    DisposableEffect(activity) {
        if (activity == null || !canCaptureScreenshots()) {
            return@DisposableEffect onDispose { }
        }

        val callback = Activity.ScreenCaptureCallback {
            viewModel.handleUiEvent(OnboardingRecoveryPhraseUiEvent.ScreenshotTaken)
        }
        activity.registerScreenCaptureCallback(activity.mainExecutor, callback)
        onDispose { activity.unregisterScreenCaptureCallback(callback) }
    }

    OnboardingRecoveryPhraseSetupContent(
        uiState = uiState,
        handleUiEvent = viewModel::handleUiEvent,
    )
}

@Composable
private fun OnboardingRecoveryPhraseSetupContent(
    uiState: OnboardingRecoveryPhraseUiState,
    handleUiEvent: (OnboardingRecoveryPhraseUiEvent) -> Unit,
) {
    val context = LocalContext.current
    val phrase = uiState.phrase

    Scaffold(
        bottomBar = {
            OnboardingNexButtonBar(
                text = if (uiState.phraseWasSaved) "Continue" else "Save QR code",
                enabled = phrase != null,
                loading = uiState.loading,
                onNext = {
                    if (uiState.phraseWasSaved) {
                        handleUiEvent(OnboardingRecoveryPhraseUiEvent.Continue)
                    } else {
                        phrase ?: return@OnboardingNexButtonBar
                        handleUiEvent(OnboardingRecoveryPhraseUiEvent.ShareQrImage(context, phrase))
                    }
                },
            )
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
                text = "Your recovery phrase"
            )

            OnboardingPageSubtitle(
                text = "The only way back into your vault if you forget your password. Save it somewhere outside this phone."
            )

            Spacer(Modifier.height(Dimens.spacingXSmall))

            RecoveryPhraseSheet(
                phrase = phrase,
                wordCount = uiState.wordCount,
                handleUiEvent = handleUiEvent,
            )
        }
    }
}

@Composable
private fun RecoveryPhraseSheet(
    phrase: RecoveryPhrase?,
    wordCount: Bip39WordCount,
    handleUiEvent: (OnboardingRecoveryPhraseUiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .recoveryPhraseChip(RoundedCornerShape(18.dp))
    ) {
        RecoveryPhraseSheetHeader(
            phrase = phrase,
            wordCount = wordCount,
            handleUiEvent = handleUiEvent,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spacingLarge)
                .padding(bottom = Dimens.spacingLarge)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                if (phrase != null) {
                    QRCodeImage(
                        text = phrase.toMnemonicString(),
                        foregroundColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(200.dp)
                    )
                } else {
                    CircularProgressIndicator()
                }
            }

            Text(
                text = phrase?.toMnemonicString()?.breakableAtDashes().orEmpty(),
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                textAlign = TextAlign.Center,
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (canCaptureScreenshots()) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            )

            Text(
                text = "Tip: a screenshot of this card works too.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.spacingMedium),
            )
        }
    }
}

@Composable
private fun RecoveryPhraseSheetHeader(
    phrase: RecoveryPhrase?,
    wordCount: Bip39WordCount,
    handleUiEvent: (OnboardingRecoveryPhraseUiEvent) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Dimens.spacingLarge, end = Dimens.spacingXSmall, top = Dimens.spacingXSmall)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Recovery phrase",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = "${wordCount.words} words",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        OtherWaysToSaveMenu(
            phrase = phrase,
            wordCount = wordCount,
            handleUiEvent = handleUiEvent,
        )
    }
}

@Composable
private fun OtherWaysToSaveMenu(
    phrase: RecoveryPhrase?,
    wordCount: Bip39WordCount,
    handleUiEvent: (OnboardingRecoveryPhraseUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current

    var expanded by remember { mutableStateOf(false) }

    val saveTextFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) {
        it ?: return@rememberLauncherForActivityResult
        phrase ?: return@rememberLauncherForActivityResult
        handleUiEvent(OnboardingRecoveryPhraseUiEvent.SaveTextFile(context, it, phrase))
    }

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true },
            enabled = phrase != null,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_more_vert),
                contentDescription = "Other ways to save",
            )
        }

        RoundedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Save as text file") },
                leadingIcon = { Icon(painterResource(R.drawable.ic_download), contentDescription = null) },
                onClick = {
                    expanded = false
                    saveTextFileLauncher.launch("photok-recovery-phrase.txt")
                },
            )
            DropdownMenuItem(
                text = { Text("Share as text") },
                leadingIcon = { Icon(painterResource(R.drawable.ic_share), contentDescription = null) },
                onClick = {
                    expanded = false
                    phrase ?: return@DropdownMenuItem
                    handleUiEvent(OnboardingRecoveryPhraseUiEvent.ShareText(context, phrase))
                },
            )
            DropdownMenuItem(
                text = { Text("Copy to clipboard") },
                leadingIcon = { Icon(painterResource(R.drawable.ic_content_copy), contentDescription = null) },
                onClick = {
                    expanded = false
                    phrase ?: return@DropdownMenuItem
                    handleUiEvent(OnboardingRecoveryPhraseUiEvent.CopyToClipboard(clipboard, phrase))
                },
            )

            HorizontalDivider()

            if (wordCount == Bip39WordCount.Twelve) {
                DropdownMenuItem(
                    text = { Text("Use 24 words instead") },
                    onClick = {
                        expanded = false
                        handleUiEvent(OnboardingRecoveryPhraseUiEvent.UpdateWordCount(Bip39WordCount.TwentyFour))
                    },
                )
            } else {
                DropdownMenuItem(
                    text = { Text("Use 12 words instead") },
                    onClick = {
                        expanded = false
                        handleUiEvent(OnboardingRecoveryPhraseUiEvent.UpdateWordCount(Bip39WordCount.Twelve))
                    },
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun OnboardingRecoveryPhraseSetupScreenPreview() {
    AppTheme {
        OnboardingRecoveryPhraseSetupContent(
            uiState = OnboardingRecoveryPhraseUiState(
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
        )
    }
}
