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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.crypto.Bip39WordCount
import dev.leonlatsch.photok.encryption.domain.models.RecoveryPhrase
import dev.leonlatsch.photok.navigation.LocalNavigator
import dev.leonlatsch.photok.onboarding.ui.OnboardingCardWindowInsets
import dev.leonlatsch.photok.onboarding.ui.OnboardingRoute
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingNexButtonBar
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingPageSubtitle
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingPageTitle
import dev.leonlatsch.photok.onboarding.ui.recoveryphrase.OnboardingRecoveryPhraseNavigationEvent
import dev.leonlatsch.photok.onboarding.ui.recoveryphrase.OnboardingRecoveryPhraseUiEvent
import dev.leonlatsch.photok.onboarding.ui.recoveryphrase.OnboardingRecoveryPhraseUiState
import dev.leonlatsch.photok.onboarding.ui.recoveryphrase.OnboardingRecoveryPhraseViewModel
import dev.leonlatsch.photok.other.AndroidFeatures
import dev.leonlatsch.photok.recovery.ui.MoreSaveOptionsMenu
import dev.leonlatsch.photok.recovery.ui.RecoveryKeyCard
import dev.leonlatsch.photok.recovery.ui.RecoveryPhraseSavedIndicator
import dev.leonlatsch.photok.recovery.ui.RecoveryPhraseSavedTo
import dev.leonlatsch.photok.ui.ObserveAsEvents
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.theme.Dimens

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
    val clipboard = LocalClipboard.current
    val phrase = uiState.phrase

    val saveTextFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) {
            it ?: return@rememberLauncherForActivityResult
            phrase ?: return@rememberLauncherForActivityResult
            handleUiEvent(OnboardingRecoveryPhraseUiEvent.SaveTextFile(it, phrase))
        }

    Scaffold(
        contentWindowInsets = OnboardingCardWindowInsets,
        bottomBar = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AnimatedVisibility(
                    visible = uiState.savedTo == null,
                ) {
                    MoreSaveOptionsMenu(
                        onShareImage = {
                            phrase ?: return@MoreSaveOptionsMenu
                            handleUiEvent(OnboardingRecoveryPhraseUiEvent.ShareImage(context, phrase))
                        },
                        onSaveTextFile = { saveTextFileLauncher.launch("photok-recovery-phrase.txt") },
                        onCopyToClipboard = {
                            phrase ?: return@MoreSaveOptionsMenu
                            handleUiEvent(OnboardingRecoveryPhraseUiEvent.CopyToClipboard(clipboard, phrase))
                        },
                    ) { closeMenu ->
                        if (uiState.wordCount == Bip39WordCount.Twelve) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.onboarding_recovery_phrase_use_24_words)) },
                                onClick = {
                                    closeMenu()
                                    handleUiEvent(OnboardingRecoveryPhraseUiEvent.UpdateWordCount(Bip39WordCount.TwentyFour))
                                },
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.onboarding_recovery_phrase_use_12_words)) },
                                onClick = {
                                    closeMenu()
                                    handleUiEvent(OnboardingRecoveryPhraseUiEvent.UpdateWordCount(Bip39WordCount.Twelve))
                                },
                            )
                        }
                    }
                }

                when {
                    uiState.savedTo != null -> {
                        OnboardingNexButtonBar(
                            text = stringResource(R.string.common_continue),
                            enabled = phrase != null,
                            loading = uiState.loading,
                            onNext = { handleUiEvent(OnboardingRecoveryPhraseUiEvent.Continue) },
                        )
                    }

                    AndroidFeatures.mediaStore() -> {
                        OnboardingNexButtonBar(
                            text = stringResource(R.string.recovery_phrase_save_to_photos),
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
                            text = stringResource(R.string.recovery_phrase_qr_save_as_image),
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
            OnboardingPageTitle(text = stringResource(R.string.recovery_phrase_label))

            OnboardingPageSubtitle(
                text = stringResource(R.string.recovery_phrase_description),
            )

            Spacer(Modifier.height(Dimens.spacingXSmall))

            RecoveryKeyCard(
                phrase = phrase,
                wordCount = uiState.wordCount,
                showQrCode = uiState.showQrCode,
                onShowQrCodeSide = { handleUiEvent(OnboardingRecoveryPhraseUiEvent.ShowQrCodeSide) },
                onShowWordsSide = { handleUiEvent(OnboardingRecoveryPhraseUiEvent.ShowWordsSide) },
                saved = uiState.savedTo != null,
                showScreenshotHint = AndroidFeatures.detectScreenRecording(),
            )

            RecoveryPhraseSavedIndicator(savedTo = uiState.savedTo)

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
