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

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.crypto.Bip39WordCount
import dev.leonlatsch.photok.encryption.domain.models.RecoveryPhrase
import dev.leonlatsch.photok.other.AndroidFeatures
import dev.leonlatsch.photok.ui.ObserveAsEvents
import dev.leonlatsch.photok.ui.components.ConfirmationDialog
import dev.leonlatsch.photok.ui.theme.Dimens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val IMAGE_FILE_NAME = "photok-recovery-phrase.jpg"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoveryPhraseSheet(
    onDismissRequest: () -> Unit,
) {
    val viewModel: RecoveryPhraseViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val phrase = uiState.phrase

    val canDismiss by rememberUpdatedState(!uiState.inputs.regenerated || uiState.inputs.savedTo != null)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { value ->
            if (value == SheetValue.Hidden && !canDismiss) {
                Toast.makeText(context, R.string.recovery_phrase_save_warning, Toast.LENGTH_SHORT).show()
                false
            } else {
                true
            }
        },
    )

    var showNewTwelveWordPhraseDialog by remember { mutableStateOf(false) }
    var showNewTwentyFourWordPhraseDialog by remember { mutableStateOf(false) }

    ConfirmationDialog(
        show = showNewTwelveWordPhraseDialog,
        onDismissRequest = { showNewTwelveWordPhraseDialog = false },
        text = stringResource(R.string.recovery_phrase_create_new_confirm),
        onConfirm = { viewModel.handleUiEvent(RecoveryPhraseUiEvent.CreateNewPhrase(Bip39WordCount.Twelve)) },
    )

    ConfirmationDialog(
        show = showNewTwentyFourWordPhraseDialog,
        onDismissRequest = { showNewTwentyFourWordPhraseDialog = false },
        text = stringResource(R.string.recovery_phrase_create_new_confirm),
        onConfirm = { viewModel.handleUiEvent(RecoveryPhraseUiEvent.CreateNewPhrase(Bip39WordCount.TwentyFour)) },
    )

    val saveImageFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/jpeg")) {
            it ?: return@rememberLauncherForActivityResult
            phrase ?: return@rememberLauncherForActivityResult
            viewModel.handleUiEvent(RecoveryPhraseUiEvent.SaveImageFile(context, it, phrase))
        }

    val saveTextFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) {
            it ?: return@rememberLauncherForActivityResult
            phrase ?: return@rememberLauncherForActivityResult
            viewModel.handleUiEvent(RecoveryPhraseUiEvent.SaveTextFile(it, phrase))
        }

    ObserveAsEvents(viewModel.navigationEvents) { event ->
        when (event) {
            RecoveryPhraseNavigationEvent.PickImageFileLocation -> saveImageFileLauncher.launch(IMAGE_FILE_NAME)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
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

            RecoveryKeyCard(
                phrase = phrase,
                wordCount = uiState.inputs.wordCount,
                showQrCode = uiState.inputs.showQrCode,
                onShowQrCodeSide = { viewModel.handleUiEvent(RecoveryPhraseUiEvent.ShowQrCodeSide) },
                onShowWordsSide = { viewModel.handleUiEvent(RecoveryPhraseUiEvent.ShowWordsSide) },
                saved = uiState.inputs.savedTo != null,
                modifier = Modifier.padding(top = Dimens.spacingLarge),
            )

            RecoveryPhraseSavedIndicator(
                savedTo = uiState.inputs.savedTo,
                modifier = Modifier.padding(top = Dimens.spacingMedium),
            )

            Spacer(Modifier.height(Dimens.spacingMedium))

            MoreSaveOptionsMenu(
                onShareImage = {
                    phrase ?: return@MoreSaveOptionsMenu
                    viewModel.handleUiEvent(RecoveryPhraseUiEvent.ShareImage(context, phrase))
                },
                onSaveTextFile = { saveTextFileLauncher.launch("photok-recovery-phrase.txt") },
                onCopyToClipboard = {
                    phrase ?: return@MoreSaveOptionsMenu
                    viewModel.handleUiEvent(RecoveryPhraseUiEvent.CopyToClipboard(clipboard, phrase))
                },
            ) { closeMenu ->
                DropdownMenuItem(
                    text = { Text("New phrase with 12 words") },
                    onClick = {
                        closeMenu()
                        showNewTwelveWordPhraseDialog = true
                    },
                )
                DropdownMenuItem(
                    text = { Text("New phrase with 24 words") },
                    onClick = {
                        closeMenu()
                        showNewTwentyFourWordPhraseDialog = true
                    },
                )
            }

            when {
                uiState.inputs.savedTo != null -> {
                    Button(
                        onClick = {
                            scope.launch { sheetState.hide() }
                                .invokeOnCompletion { onDismissRequest() }
                        },
                        modifier = Modifier
                            .navigationBarsPadding()
                            .fillMaxWidth(),
                    ) {
                        Text("Done")
                    }
                }

                AndroidFeatures.mediaStore() -> {
                    Button(
                        onClick = {
                            phrase ?: return@Button
                            viewModel.handleUiEvent(RecoveryPhraseUiEvent.SaveImageToPhotos(context, phrase))
                        },
                        enabled = phrase != null && !uiState.inputs.loading,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .fillMaxWidth(),
                    ) {
                        Text("Save to Photos")
                    }
                }

                else -> {
                    Button(
                        onClick = { saveImageFileLauncher.launch(IMAGE_FILE_NAME) },
                        enabled = phrase != null && !uiState.inputs.loading,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .fillMaxWidth(),
                    ) {
                        Text("Save as image")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// Vertical padding needed because animateContentSize() clips animation
@Composable
fun RecoveryPhraseFlowRow(
    phrase: RecoveryPhrase?,
    animated: Boolean,
    verticalPadding: Dp = Dimens.spacingLarge,
    modifier: Modifier = Modifier,
) {
    val isPreview = LocalInspectionMode.current

    var words by remember {
        mutableStateOf(phrase?.words.orEmpty())
    }

    LaunchedEffect(phrase) {
        if (phrase != null) {
            words = phrase.words
        }
    }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = if (words.size > 12) 4 else 3,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(vertical = verticalPadding)
    ) {
        words.forEachIndexed { index, word ->
            var show by remember(phrase, animated) {
                mutableStateOf(!animated || isPreview)
            }

            LaunchedEffect(phrase) {
                if (animated) {
                    delay(300L)

                    delay((index.toLong() + 1) * 20)
                    show = true
                }
            }

            val scale by animateFloatAsState(
                targetValue = if (show) 1f else 0f,
                animationSpec = spring(
                    dampingRatio = 0.6f,
                    stiffness = 200f
                ),
                visibilityThreshold = 0.01f,
            )

            WordChip(
                number = index + 1,
                word = word,
                modifier = Modifier.graphicsLayer {
                    this.scaleX = scale
                    this.scaleY = scale
                    this.alpha = scale
                }
            )
        }
    }
}

@Composable
internal fun WordChip(
    number: Int,
    word: String,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .graphicsLayer { rotationZ = rotation.value }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                scope.launch {
                    if (rotation.isRunning) rotation.stop()
                    rotation.snapTo(12f)
                    rotation.animateTo(0f, spring(dampingRatio = 0.3f, stiffness = 500f))
                }
            }
            .recoveryPhraseChip(RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$number",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = word,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun Modifier.recoveryPhraseChip(shape: Shape): Modifier = this
    .background(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = shape,
    )
    .border(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        shape = shape,
    )
