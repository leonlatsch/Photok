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
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateBounds
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.LookaheadScope
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
import dev.leonlatsch.photok.onboarding.ui.recoveryphrase.OnboardingRecoveryPhraseNavigationEvent
import dev.leonlatsch.photok.onboarding.ui.recoveryphrase.OnboardingRecoveryPhraseUiEvent
import dev.leonlatsch.photok.onboarding.ui.recoveryphrase.OnboardingRecoveryPhraseUiState
import dev.leonlatsch.photok.onboarding.ui.recoveryphrase.OnboardingRecoveryPhraseViewModel
import dev.leonlatsch.photok.onboarding.ui.recoveryphrase.RecoveryPhraseSavedTo
import dev.leonlatsch.photok.other.AndroidFeatures
import dev.leonlatsch.photok.recovery.ui.breakableAtDashes
import dev.leonlatsch.photok.recovery.ui.recoveryPhraseChip
import dev.leonlatsch.photok.ui.ObserveAsEvents
import dev.leonlatsch.photok.ui.components.RoundedDropdownMenu
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.theme.Dimens
import dev.leonlatsch.photok.uicomponnets.qr.QRCodeImage

private const val IMAGE_FILE_NAME = "photok-recovery-phrase.jpg"


@Composable
fun OnboardingRecoveryPhraseSetupScreen() {
    val viewModel: OnboardingRecoveryPhraseViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current
    val context = LocalContext.current

    val saveImageFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/jpeg")) {
            it ?: return@rememberLauncherForActivityResult
            val phrase = uiState.phrase ?: return@rememberLauncherForActivityResult
            viewModel.handleUiEvent(
                OnboardingRecoveryPhraseUiEvent.SaveImageFile(
                    context,
                    it,
                    phrase
                )
            )
        }

    ObserveAsEvents(viewModel.navigationEvents) { event ->
        when (event) {
            OnboardingRecoveryPhraseNavigationEvent.Continue -> navigator.navigate(OnboardingRoute.AddFirstPhotos)
            OnboardingRecoveryPhraseNavigationEvent.PickImageFileLocation -> saveImageFileLauncher.launch(
                IMAGE_FILE_NAME
            )
        }
    }

    LifecycleResumeEffect(Unit) {
        if (AndroidFeatures.detectScreenRecording()) {
            viewModel.handleUiEvent(OnboardingRecoveryPhraseUiEvent.AllowScreenshots)
        }
        onPauseOrDispose { viewModel.handleUiEvent(OnboardingRecoveryPhraseUiEvent.RevokeScreenshots) }
    }

    val activity = LocalActivity.current
    DisposableEffect(activity) {
        if (activity == null || !AndroidFeatures.detectScreenRecording()) {
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
        onSaveImageAsFile = { saveImageFileLauncher.launch(IMAGE_FILE_NAME) },
    )
}

@Composable
private fun OnboardingRecoveryPhraseSetupContent(
    uiState: OnboardingRecoveryPhraseUiState,
    handleUiEvent: (OnboardingRecoveryPhraseUiEvent) -> Unit,
    onSaveImageAsFile: () -> Unit,
) {
    val context = LocalContext.current
    val phrase = uiState.phrase

    Scaffold(
        bottomBar = {
            Column {
                when {
                    uiState.savedTo != null -> {
                        OnboardingNexButtonBar(
                            text = "Continue",
                            enabled = phrase != null,
                            loading = uiState.loading,
                            onNext = { handleUiEvent(OnboardingRecoveryPhraseUiEvent.Continue) },
                        )
                    }

                    AndroidFeatures.mediaStore() -> {
                        OnboardingNexButtonBar(
                            text = "Save to Photos",
                            enabled = phrase != null,
                            loading = uiState.loading,
                            onNext = {
                                phrase ?: return@OnboardingNexButtonBar
                                handleUiEvent(
                                    OnboardingRecoveryPhraseUiEvent.SaveImageToPhotos(
                                        context,
                                        phrase
                                    )
                                )
                            },
                        )
                    }

                    else -> {
                        OnboardingNexButtonBar(
                            text = "Save as image",
                            enabled = phrase != null,
                            loading = uiState.loading,
                            onNext = onSaveImageAsFile,
                        )
                    }
                }
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
            OnboardingPageSubtitle(
                text = "Keep these words off this phone. They unlock your vault if you forget your password.",
            )

            Spacer(Modifier.height(Dimens.spacingXSmall))

            RecoveryPhraseCard(
                phrase = phrase,
                saved = uiState.savedTo != null,
            )

            RecoveryPhraseSavedIndicator(savedTo = uiState.savedTo)

            AnimatedVisibility(
                visible = uiState.savedTo == null,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                OtherWaysToSaveMenu(
                    phrase = phrase,
                    wordCount = uiState.wordCount,
                    handleUiEvent = handleUiEvent,
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun RecoveryPhraseCard(
    phrase: RecoveryPhrase?,
    saved: Boolean,
) {
    val qrCode = remember {
        movableContentOf { shownPhrase: RecoveryPhrase?, modifier: Modifier ->
            RecoveryPhraseQrCode(phrase = shownPhrase, modifier = modifier)
        }
    }
    val phraseText = remember {
        movableContentOf { shownPhrase: RecoveryPhrase?, textAlign: TextAlign, modifier: Modifier ->
            RecoveryPhraseText(phrase = shownPhrase, textAlign = textAlign, modifier = modifier)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .recoveryPhraseChip(RoundedCornerShape(18.dp))
    ) {
        LookaheadScope {
            if (saved) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spacingMedium)
                ) {
                    qrCode(
                        phrase,
                        Modifier
                            .animateBounds(this@LookaheadScope)
                            .size(96.dp)
                    )
                    phraseText(
                        phrase,
                        TextAlign.Start,
                        Modifier
                            .animateBounds(this@LookaheadScope)
                            .weight(1f)
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spacingLarge)
                ) {
                    qrCode(
                        phrase,
                        Modifier
                            .animateBounds(this@LookaheadScope)
                            .size(200.dp)
                    )
                    phraseText(
                        phrase,
                        TextAlign.Center,
                        Modifier
                            .animateBounds(this@LookaheadScope)
                            .fillMaxWidth()
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = AndroidFeatures.detectScreenRecording() && !saved,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                )

                Text(
                    text = "Or just take a screenshot",
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
}

@Composable
private fun RecoveryPhraseQrCode(
    phrase: RecoveryPhrase?,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = phrase,
        transitionSpec = {
            (fadeIn(tween(400)) + scaleIn(tween(400), initialScale = 0.92f)) togetherWith
                fadeOut(tween(200))
        },
        contentAlignment = Alignment.Center,
        label = "RecoveryPhraseQrCode",
        modifier = modifier,
    ) { shownPhrase ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (shownPhrase != null) {
                QRCodeImage(
                    text = shownPhrase.toMnemonicString(),
                    foregroundColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun RecoveryPhraseText(
    phrase: RecoveryPhrase?,
    textAlign: TextAlign,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = phrase,
        transitionSpec = {
            (fadeIn(tween(400, delayMillis = 150)) +
                slideInVertically(tween(400, delayMillis = 150)) { it / 4 }) togetherWith
                fadeOut(tween(200))
        },
        label = "RecoveryPhraseText",
        modifier = modifier,
    ) { shownPhrase ->
        Text(
            text = shownPhrase?.toMnemonicString()?.breakableAtDashes().orEmpty(),
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            textAlign = textAlign,
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
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

    val saveTextFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) {
            it ?: return@rememberLauncherForActivityResult
            phrase ?: return@rememberLauncherForActivityResult
            handleUiEvent(OnboardingRecoveryPhraseUiEvent.SaveTextFile(context, it, phrase))
        }

    Box(modifier = modifier) {
        TextButton(
            onClick = { expanded = true }
        ) {
            Text("More options")
        }

        RoundedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Share as image") },
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.ic_share),
                        contentDescription = null
                    )
                },
                onClick = {
                    expanded = false
                    phrase ?: return@DropdownMenuItem
                    handleUiEvent(OnboardingRecoveryPhraseUiEvent.ShareImage(context, phrase))
                },
            )
            DropdownMenuItem(
                text = { Text("Save as text file") },
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.ic_download),
                        contentDescription = null
                    )
                },
                onClick = {
                    expanded = false
                    saveTextFileLauncher.launch("photok-recovery-phrase.txt")
                },
            )
            DropdownMenuItem(
                text = { Text("Copy to clipboard") },
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.ic_content_copy),
                        contentDescription = null
                    )
                },
                onClick = {
                    expanded = false
                    phrase ?: return@DropdownMenuItem
                    handleUiEvent(
                        OnboardingRecoveryPhraseUiEvent.CopyToClipboard(
                            clipboard,
                            phrase
                        )
                    )
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

@Composable
private fun RecoveryPhraseSavedIndicator(
    savedTo: RecoveryPhraseSavedTo?,
    modifier: Modifier = Modifier,
) {
    var lastSavedTo by remember { mutableStateOf(savedTo) }
    if (savedTo != null) lastSavedTo = savedTo

    AnimatedVisibility(
        visible = savedTo != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier,
    ) {
        when (val shown = lastSavedTo) {
            RecoveryPhraseSavedTo.Photos -> SavedIndicatorRow(
                title = "Saved to Photos",
                description = "Look for it in the Photok album (Pictures/Photok)",
            )

            is RecoveryPhraseSavedTo.File -> SavedIndicatorRow(
                title = if (shown.fileName != null) "Saved to ${shown.fileName}" else "Saved as file",
                description = "Find it in the folder you picked",
            )

            is RecoveryPhraseSavedTo.Shared -> SavedIndicatorRow(
                title = if (shown.appName != null) "Shared with ${shown.appName}" else "Shared",
                description = "Make sure it arrived where you sent it",
            )

            RecoveryPhraseSavedTo.Clipboard -> SavedIndicatorRow(
                title = "Copied to clipboard",
                description = "Paste it somewhere safe outside this phone",
            )

            RecoveryPhraseSavedTo.Screenshot -> SavedIndicatorRow(
                title = "Screenshot taken",
                description = "Look for it in your Screenshots album",
            )

            null -> Unit
        }
    }
}

@Composable
private fun SavedIndicatorRow(
    title: String,
    description: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(Dimens.spacingMedium),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_check_circle),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
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
            onSaveImageAsFile = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun OnboardingRecoveryPhraseSetupScreenSavedPreview() {
    AppTheme {
        OnboardingRecoveryPhraseSetupContent(
            uiState = OnboardingRecoveryPhraseUiState(
                phrase = RecoveryPhrase(List(12) { "word" }),
                savedTo = RecoveryPhraseSavedTo.Photos,
            ),
            handleUiEvent = {},
            onSaveImageAsFile = {},
        )
    }
}
