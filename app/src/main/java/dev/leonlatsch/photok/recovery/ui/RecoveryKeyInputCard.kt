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

package dev.leonlatsch.photok.recovery.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.crypto.Bip39WordCount
import dev.leonlatsch.photok.ui.components.NoKeyboardLearning
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.theme.Dimens
import dev.leonlatsch.photok.uicomponnets.qr.QrScannerView
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun RecoveryKeyInputCard(
    words: List<String>,
    wordCount: Bip39WordCount,
    unknownWordIndices: Set<Int>,
    showScanner: Boolean,
    unlocked: Boolean,
    revealToken: Int,
    enabled: Boolean,
    onUpdateWord: (index: Int, word: String) -> Unit,
    onFillWords: (startIndex: Int, words: List<String>) -> Unit,
    onSelectWordCount: (Bip39WordCount) -> Unit,
    onQrScanned: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (showScanner || unlocked) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow),
    )

    val reveal = remember { Animatable(Bip39WordCount.TwentyFour.words.toFloat()) }
    LaunchedEffect(revealToken) {
        if (revealToken == 0) return@LaunchedEffect
        reveal.snapTo(0f)
        reveal.animateTo(
            targetValue = words.size.toFloat(),
            animationSpec = tween(durationMillis = words.size * 70, easing = LinearEasing),
        )
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = colorScheme.primary,
        contentColor = colorScheme.surface,
        shadowElevation = 10.dp,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
    ) {
        Box {
            RecoveryKeyInputWordsFace(
                words = words,
                wordCount = wordCount,
                unknownWordIndices = unknownWordIndices,
                enabled = enabled && !showScanner,
                reveal = { reveal.value },
                onUpdateWord = onUpdateWord,
                onFillWords = onFillWords,
                onSelectWordCount = onSelectWordCount,
                modifier = Modifier.graphicsLayer {
                    alpha = if (rotation <= 90f) 1f else 0f
                },
            )
            if (unlocked) {
                RecoveryKeyUnlockedFace(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            alpha = if (rotation > 90f) 1f else 0f
                            rotationY = 180f
                        },
                )
            } else {
                RecoveryKeyScannerFace(
                    showScanner = showScanner,
                    onQrScanned = onQrScanned,
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            alpha = if (rotation > 90f) 1f else 0f
                            rotationY = 180f
                        },
                )
            }
        }
    }
}

@Composable
private fun RecoveryKeyInputWordsFace(
    words: List<String>,
    wordCount: Bip39WordCount,
    unknownWordIndices: Set<Int>,
    enabled: Boolean,
    reveal: () -> Float,
    onUpdateWord: (index: Int, word: String) -> Unit,
    onFillWords: (startIndex: Int, words: List<String>) -> Unit,
    onSelectWordCount: (Bip39WordCount) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
        modifier = modifier.padding(Dimens.spacingLarge),
    ) {
        RecoveryKeyFaceHeader(label = "RECOVERY KEY") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(LocalContentColor.current.copy(alpha = 0.14f))
                    .padding(3.dp),
            ) {
                WordCountPill(
                    text = "12 words",
                    selected = wordCount == Bip39WordCount.Twelve,
                    enabled = enabled,
                    onClick = { onSelectWordCount(Bip39WordCount.Twelve) },
                )
                WordCountPill(
                    text = "24 words",
                    selected = wordCount == Bip39WordCount.TwentyFour,
                    enabled = enabled,
                    onClick = { onSelectWordCount(Bip39WordCount.TwentyFour) },
                )
            }
        }

        NoKeyboardLearning {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                words.indices.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        row.forEach { index ->
                            RecoveryWordInput(
                                number = index + 1,
                                word = words[index],
                                unknown = index in unknownWordIndices,
                                isLast = index == words.lastIndex,
                                enabled = enabled,
                                revealProgress = { (reveal() - index).coerceIn(0f, 1f) },
                                onUpdateWord = { onUpdateWord(index, it) },
                                onFillWords = { onFillWords(index, it) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WordCountPill(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val background by animateColorAsState(
        if (selected) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0f)
    )
    val textColor by animateColorAsState(
        if (selected) colorScheme.primary else LocalContentColor.current
    )

    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = textColor,
        modifier = Modifier
            .clip(CircleShape)
            .background(background)
            .clickable(enabled = enabled && !selected, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
private fun RecoveryWordInput(
    number: Int,
    word: String,
    unknown: Boolean,
    isLast: Boolean,
    enabled: Boolean,
    revealProgress: () -> Float,
    onUpdateWord: (String) -> Unit,
    onFillWords: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    val background by animateColorAsState(
        if (unknown) colorScheme.errorContainer else LocalContentColor.current.copy(alpha = 0.14f)
    )
    val textColor by animateColorAsState(
        if (unknown) colorScheme.onErrorContainer else LocalContentColor.current
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .padding(horizontal = Dimens.spacingSmall, vertical = 6.dp),
    ) {
        Text(
            text = number.toString().padStart(2, '0'),
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = textColor,
            modifier = Modifier.alpha(0.5f),
        )
        BasicTextField(
            value = word,
            onValueChange = { raw ->
                val parts = raw.toRecoveryPhraseWords()
                when {
                    parts.size > 1 -> {
                        onFillWords(parts)
                        focusManager.clearFocus()
                    }

                    raw.isNotEmpty() && raw.last().isWhitespace() -> {
                        onUpdateWord(parts.firstOrNull().orEmpty())
                        if (isLast) focusManager.clearFocus() else focusManager.moveFocus(FocusDirection.Next)
                    }

                    else -> onUpdateWord(parts.firstOrNull().orEmpty())
                }
            },
            enabled = enabled,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = textColor,
            ),
            cursorBrush = SolidColor(textColor),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.PasswordVisible,
                imeAction = if (isLast) ImeAction.Done else ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Next) },
                onDone = { focusManager.clearFocus() },
            ),
            modifier = Modifier
                .weight(1f)
                .graphicsLayer {
                    val progress = revealProgress()
                    alpha = progress
                    translationY = (1f - progress) * 6.dp.toPx()
                },
        )
    }
}

@Composable
private fun RecoveryKeyScannerFace(
    showScanner: Boolean,
    onQrScanned: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
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
            if (showScanner) {
                QrScannerView(
                    onQrCodeDecoded = onQrScanned,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f),
                )
            }
        }
    }
}

@Composable
private fun RecoveryKeyUnlockedFace(
    modifier: Modifier = Modifier,
) {
    val isPreview = LocalInspectionMode.current
    val iconScale = remember { Animatable(if (isPreview) 1f else 0f) }
    LaunchedEffect(Unit) {
        delay(350.milliseconds)
        iconScale.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessLow))
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
        modifier = modifier.padding(Dimens.spacingLarge),
    ) {
        RecoveryKeyFaceHeader(label = "VAULT UNLOCKED") {
            Icon(
                painter = painterResource(R.drawable.ic_lock),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall, Alignment.CenterVertically),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check_circle),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer {
                        scaleX = iconScale.value
                        scaleY = iconScale.value
                    },
            )
            Text(
                text = stringResource(R.string.recovery_phrase_restore_unlocked),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Opening your vault",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alpha(0.7f),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun RecoveryKeyInputCardPreview() {
    AppTheme {
        Surface {
            RecoveryKeyInputCard(
                words = listOf("river", "apple", "orbit", "candle", "frozxx", "maple", "tunnel", "", "", "", "", ""),
                wordCount = Bip39WordCount.Twelve,
                unknownWordIndices = setOf(4),
                showScanner = false,
                unlocked = false,
                revealToken = 0,
                enabled = true,
                onUpdateWord = { _, _ -> },
                onFillWords = { _, _ -> },
                onSelectWordCount = {},
                onQrScanned = {},
                modifier = Modifier.padding(Dimens.spacingLarge),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun RecoveryKeyInputCardScannerPreview() {
    AppTheme {
        Surface {
            RecoveryKeyInputCard(
                words = List(12) { "" },
                wordCount = Bip39WordCount.Twelve,
                unknownWordIndices = emptySet(),
                showScanner = true,
                unlocked = false,
                revealToken = 0,
                enabled = true,
                onUpdateWord = { _, _ -> },
                onFillWords = { _, _ -> },
                onSelectWordCount = {},
                onQrScanned = {},
                modifier = Modifier.padding(Dimens.spacingLarge),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun RecoveryKeyInputCardUnlockedPreview() {
    AppTheme {
        Surface {
            RecoveryKeyInputCard(
                words = List(12) { "word" },
                wordCount = Bip39WordCount.Twelve,
                unknownWordIndices = emptySet(),
                showScanner = false,
                unlocked = true,
                revealToken = 0,
                enabled = false,
                onUpdateWord = { _, _ -> },
                onFillWords = { _, _ -> },
                onSelectWordCount = {},
                onQrScanned = {},
                modifier = Modifier.padding(Dimens.spacingLarge),
            )
        }
    }
}
