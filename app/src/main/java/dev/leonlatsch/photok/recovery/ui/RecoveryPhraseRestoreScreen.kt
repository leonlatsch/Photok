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

package dev.leonlatsch.photok.recovery.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.crypto.Bip39WordCount
import dev.leonlatsch.photok.navigation.LocalNavigator
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingNexButtonBar
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingPageSubtitle
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingPageTitle
import dev.leonlatsch.photok.other.shouldRequestPermissionInSettings
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.theme.Dimens
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun RecoveryPhraseRestoreScreen(
    onUnlocked: () -> Unit,
) {
    val viewModel: RecoveryPhraseRestoreViewModel = hiltViewModel()
    val activity = LocalActivity.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.unlocked) {
        if (uiState.unlocked) {
            delay(3.seconds)
            activity?.let(viewModel::requestInAppReview)
            onUnlocked()
        }
    }

    RecoveryPhraseRestoreContent(
        uiState = uiState,
        handleUiEvent = viewModel::handleUiEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecoveryPhraseRestoreContent(
    uiState: RecoveryPhraseRestoreUiState,
    handleUiEvent: (RecoveryPhraseRestoreUiEvent) -> Unit,
) {
    val navigator = LocalNavigator.current
    val clipboard = LocalClipboard.current
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val activity = LocalActivity.current

    var shouldRequestCameraPermissionInSettings by remember {
        mutableStateOf(activity?.shouldRequestPermissionInSettings(Manifest.permission.CAMERA) == true)
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            handleUiEvent(RecoveryPhraseRestoreUiEvent.ShowScanner)
        } else {
            shouldRequestCameraPermissionInSettings =
                activity?.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) == false
        }
    }

    val selectFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        handleUiEvent(RecoveryPhraseRestoreUiEvent.LoadFromFile(uri))
    }

    BackHandler(uiState.showScanner) {
        handleUiEvent(RecoveryPhraseRestoreUiEvent.ShowWords)
    }

    val inputEnabled = !uiState.loading && !uiState.unlocked

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = navigator::goBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = stringResource(R.string.common_cancel),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        bottomBar = {
            OnboardingNexButtonBar(
                text = stringResource(R.string.recovery_phrase_restore_button),
                enabled = uiState.phraseValid && !uiState.unlocked,
                loading = uiState.loading && !uiState.unlocked,
                onNext = {
                    focusManager.clearFocus()
                    handleUiEvent(RecoveryPhraseRestoreUiEvent.Unlock)
                },
            )
        },
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.spacingLarge)
                .padding(bottom = Dimens.spacingLarge),
        ) {
            OnboardingPageTitle(text = stringResource(R.string.recovery_phrase_forgot_password))

            OnboardingPageSubtitle(
                text = "Enter the words of your recovery phrase to unlock your vault.",
            )

            Spacer(Modifier.height(Dimens.spacingXSmall))

            RecoveryKeyInputCard(
                words = uiState.words,
                wordCount = uiState.wordCount,
                unknownWordIndices = uiState.unknownWordIndices,
                showScanner = uiState.showScanner,
                unlocked = uiState.unlocked,
                revealToken = uiState.revealToken,
                enabled = inputEnabled,
                onUpdateWord = { index, word ->
                    handleUiEvent(
                        RecoveryPhraseRestoreUiEvent.UpdateWord(
                            index,
                            word
                        )
                    )
                },
                onFillWords = { index, words ->
                    handleUiEvent(
                        RecoveryPhraseRestoreUiEvent.FillWords(
                            index,
                            words
                        )
                    )
                },
                onSelectWordCount = { handleUiEvent(RecoveryPhraseRestoreUiEvent.SelectWordCount(it)) },
                onQrScanned = { handleUiEvent(RecoveryPhraseRestoreUiEvent.QrScanned(it)) },
                modifier = Modifier.imePadding()
            )


            AnimatedVisibility(
                !uiState.unlocked
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
                    modifier = Modifier.height(IntrinsicSize.Min),
                ) {
                    if (uiState.showScanner) {
                        RestoreSourceButton(
                            text = "Type words",
                            icon = R.drawable.ic_keyboard,
                            enabled = inputEnabled,
                            onClick = { handleUiEvent(RecoveryPhraseRestoreUiEvent.ShowWords) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    } else {
                        RestoreSourceButton(
                            text = stringResource(R.string.recovery_phrase_restore_option_scan_qr),
                            icon = R.drawable.ic_qr_code,
                            enabled = inputEnabled,
                            onClick = {
                                focusManager.clearFocus()
                                when {
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                                        handleUiEvent(RecoveryPhraseRestoreUiEvent.ShowScanner)
                                    }

                                    shouldRequestCameraPermissionInSettings -> {
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        }
                                        context.startActivity(intent)
                                    }

                                    else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }
                    RestoreSourceButton(
                        text = stringResource(R.string.recovery_phrase_restore_option_paste),
                        icon = R.drawable.ic_paste,
                        enabled = inputEnabled,
                        onClick = {
                            focusManager.clearFocus()
                            handleUiEvent(RecoveryPhraseRestoreUiEvent.PasteFromClipboard(clipboard))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                    RestoreSourceButton(
                        text = stringResource(R.string.recovery_phrase_restore_option_open_file),
                        icon = R.drawable.ic_upload,
                        enabled = inputEnabled,
                        onClick = {
                            focusManager.clearFocus()
                            selectFileLauncher.launch(arrayOf("text/plain"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.source != null && !uiState.unlocked,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Text(
                    text = uiState.source.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            AnimatedVisibility(
                visible = uiState.error != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Text(
                    text = uiState.error.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun RestoreSourceButton(
    text: String,
    icon: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        color = colorScheme.secondaryContainer,
        contentColor = colorScheme.onSecondaryContainer,
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingXSmall, Alignment.CenterVertically),
            modifier = Modifier.padding(
                vertical = Dimens.spacingMedium,
                horizontal = Dimens.spacingSmall
            ),
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}

private val PREVIEW_WORDS = listOf(
    "river",
    "apple",
    "orbit",
    "candle",
    "frozen",
    "maple",
    "tunnel",
    "quiet",
    "velvet",
    "harbor",
    "pixel",
    "summit"
)

@PreviewLightDark
@Composable
private fun RecoveryPhraseRestoreEmptyPreview() {
    AppTheme {
        RecoveryPhraseRestoreContent(
            uiState = RecoveryPhraseRestoreUiState(),
            handleUiEvent = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun RecoveryPhraseRestorePastedPreview() {
    AppTheme {
        RecoveryPhraseRestoreContent(
            uiState = RecoveryPhraseRestoreUiState(
                words = PREVIEW_WORDS,
                wordCount = Bip39WordCount.Twelve,
                source = "Pasted from clipboard",
                phraseValid = true,
            ),
            handleUiEvent = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun RecoveryPhraseRestoreScannerPreview() {
    AppTheme {
        RecoveryPhraseRestoreContent(
            uiState = RecoveryPhraseRestoreUiState(showScanner = true),
            handleUiEvent = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun RecoveryPhraseRestoreUnlockedPreview() {
    AppTheme {
        RecoveryPhraseRestoreContent(
            uiState = RecoveryPhraseRestoreUiState(
                words = PREVIEW_WORDS,
                wordCount = Bip39WordCount.Twelve,
                phraseValid = true,
                loading = true,
                unlocked = true,
            ),
            handleUiEvent = {},
        )
    }
}
