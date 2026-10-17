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
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import dev.leonlatsch.photok.onboarding.ui.recoveryphrase.RecoveryPhraseSavedTo
import dev.leonlatsch.photok.other.AndroidFeatures
import dev.leonlatsch.photok.ui.ObserveAsEvents
import dev.leonlatsch.photok.ui.components.RoundedDropdownMenu
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.theme.Dimens
import dev.leonlatsch.photok.uicomponnets.qr.QRCodeImage
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AnimatedVisibility(
                    visible = uiState.savedTo == null,
                ) {
                    OtherWaysToSaveMenu(
                        phrase = phrase,
                        wordCount = uiState.wordCount,
                        handleUiEvent = handleUiEvent,
                    )
                }

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
            OnboardingPageTitle(text = "Recovery phrase")

            OnboardingPageSubtitle(
                text = "Keep these words off this phone. They unlock your vault if you forget your password.",
            )

            Spacer(Modifier.height(Dimens.spacingXSmall))

            RecoveryKeyCard(
                phrase = phrase,
                wordCount = uiState.wordCount,
                saved = uiState.savedTo != null,
                showQrCode = uiState.showQrCode,
                handleUiEvent = handleUiEvent,
            )

            RecoveryPhraseSavedIndicator(savedTo = uiState.savedTo)

        }
    }
}

@Composable
private fun RecoveryKeyCard(
    phrase: RecoveryPhrase?,
    wordCount: Bip39WordCount,
    saved: Boolean,
    showQrCode: Boolean,
    handleUiEvent: (OnboardingRecoveryPhraseUiEvent) -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (showQrCode) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow),
    )

    val reveal = remember(phrase) { Animatable(0f) }
    LaunchedEffect(phrase) {
        if (phrase != null) {
            reveal.animateTo(
                targetValue = phrase.words.size.toFloat(),
                animationSpec = tween(durationMillis = phrase.words.size * 70, easing = LinearEasing),
            )
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
    ) {
        Box {
            Surface(
                onClick = {
                    if (showQrCode) {
                        handleUiEvent(OnboardingRecoveryPhraseUiEvent.ShowWordsSide)
                    } else {
                        handleUiEvent(OnboardingRecoveryPhraseUiEvent.ShowQrCodeSide)
                    }
                },
                enabled = phrase != null,
                shape = RoundedCornerShape(24.dp),
                color = colorScheme.primaryContainer,
                contentColor = colorScheme.onPrimaryContainer,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    },
            ) {
                Box(modifier = Modifier.animateContentSize()) {
                    RecoveryKeyWordsFace(
                        phrase = phrase,
                        wordCount = wordCount,
                        reveal = { reveal.value },
                        modifier = Modifier.graphicsLayer {
                            alpha = if (rotation <= 90f) 1f else 0f
                        },
                    )
                    RecoveryKeyQrFace(
                        phrase = phrase,
                        showScreenshotHint = AndroidFeatures.detectScreenRecording() && !saved,
                        modifier = Modifier
                            .matchParentSize()
                            .graphicsLayer {
                                alpha = if (rotation > 90f) 1f else 0f
                                rotationY = 180f
                            },
                    )
                }
            }

            SavedStamp(
                visible = saved,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-12).dp, y = 14.dp),
            )
        }

        AnimatedContent(
            targetState = showQrCode,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "RecoveryKeyFlipHint",
        ) { shownQrCode ->
            Text(
                text = if (shownQrCode) "Tap the card to show as words" else "Tap the card to show as QR code",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RecoveryKeyWordsFace(
    phrase: RecoveryPhrase?,
    wordCount: Bip39WordCount,
    reveal: () -> Float,
    modifier: Modifier = Modifier,
) {
    val count = phrase?.words?.size ?: wordCount.words

    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
        modifier = modifier.padding(Dimens.spacingLarge),
    ) {
        RecoveryKeyFaceHeader(label = "RECOVERY KEY") {
            Text(
                text = "$count words",
                style = MaterialTheme.typography.labelMedium,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            (0 until count).chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { index ->
                        RecoveryWord(
                            number = index + 1,
                            word = phrase?.words?.getOrNull(index),
                            revealProgress = { (reveal() - index).coerceIn(0f, 1f) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecoveryKeyQrFace(
    phrase: RecoveryPhrase?,
    showScreenshotHint: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
        modifier = modifier.padding(Dimens.spacingLarge),
    ) {
        RecoveryKeyFaceHeader(label = "SCAN TO RESTORE") {
            Icon(
                painter = painterResource(R.drawable.ic_qr_code),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            if (phrase != null) {
                QRCodeImage(
                    text = phrase.toMnemonicString(),
                    foregroundColor = colorScheme.onSurface,
                    backgroundColor = colorScheme.surface,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colorScheme.surface)
                        .padding(Dimens.spacingSmall),
                )
            }
        }

        AnimatedVisibility(
            visible = showScreenshotHint,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Text(
                text = "You can also take a screenshot of the QR code",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.alpha(0.7f),
            )
        }
    }
}

@Composable
private fun RecoveryKeyFaceHeader(
    label: String,
    trailing: @Composable () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier
                .weight(1f)
                .alpha(0.7f),
        )
        trailing()
    }
}

@Composable
private fun RecoveryWord(
    number: Int,
    word: String?,
    revealProgress: () -> Float,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colorScheme.surface.copy(alpha = 0.6f))
            .padding(horizontal = Dimens.spacingSmall, vertical = 6.dp),
    ) {
        Text(
            text = number.toString().padStart(2, '0'),
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier.alpha(0.5f),
        )
        Text(
            text = word.orEmpty(),
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.graphicsLayer {
                val progress = revealProgress()
                alpha = progress
                translationY = (1f - progress) * 6.dp.toPx()
            },
        )
    }
}

@Composable
private fun SavedStamp(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow),
    )

    LaunchedEffect(visible) {
        if (visible) {
            delay(120.milliseconds)
            haptics.performHapticFeedback(HapticFeedbackType.Confirm)
        }
    }

    val shape = RoundedCornerShape(8.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .graphicsLayer {
                alpha = progress.coerceIn(0f, 1f)
                val scale = 1.8f - 0.8f * progress
                scaleX = scale
                scaleY = scale
                rotationZ = -10f
            }
            .background(colorScheme.surface, shape)
            .border(2.dp, colorScheme.primary, shape)
            .padding(horizontal = Dimens.spacingSmall, vertical = 4.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_check),
            contentDescription = null,
            tint = colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = "SAVED",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            color = colorScheme.primary,
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
                description = "Look for it in Pictures/Photok. If your gallery backs up to the cloud, the image is uploaded too",
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
            .background(colorScheme.secondaryContainer)
            .padding(Dimens.spacingMedium),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_check_circle),
            contentDescription = null,
            tint = colorScheme.onPrimaryContainer,
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colorScheme.onPrimaryContainer,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onPrimaryContainer,
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
