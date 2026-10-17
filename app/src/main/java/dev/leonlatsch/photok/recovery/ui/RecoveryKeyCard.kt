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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.crypto.Bip39WordCount
import dev.leonlatsch.photok.encryption.domain.models.RecoveryPhrase
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.theme.Dimens
import dev.leonlatsch.photok.uicomponnets.qr.QRCodeImage
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun RecoveryKeyCard(
    phrase: RecoveryPhrase?,
    wordCount: Bip39WordCount,
    showQrCode: Boolean,
    onShowQrCodeSide: () -> Unit,
    onShowWordsSide: () -> Unit,
    modifier: Modifier = Modifier,
    saved: Boolean = false,
    showScreenshotHint: Boolean = false,
) {
    val rotation by animateFloatAsState(
        targetValue = if (showQrCode) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow),
    )

    var hasFlipped by rememberSaveable { mutableStateOf(false) }
    val peek = remember { Animatable(0f) }
    LaunchedEffect(hasFlipped, phrase != null) {
        if (hasFlipped || phrase == null) {
            peek.animateTo(0f)
            return@LaunchedEffect
        }
        while (true) {
            delay(2500.milliseconds)
            peek.animateTo(16f, tween(durationMillis = 280, easing = FastOutSlowInEasing))
            peek.animateTo(0f, spring(dampingRatio = 0.3f, stiffness = Spring.StiffnessMediumLow))
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
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
        modifier = modifier,
    ) {
        Box {
            Surface(
                onClick = {
                    hasFlipped = true
                    if (showQrCode) {
                        onShowWordsSide()
                    } else {
                        onShowQrCodeSide()
                    }
                },
                enabled = phrase != null,
                interactionSource = interactionSource,
                shape = RoundedCornerShape(24.dp),
                color = colorScheme.primary,
                contentColor = colorScheme.surface,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        rotationY = rotation + peek.value
                        cameraDistance = 12f * density
                        scaleX = pressScale
                        scaleY = pressScale
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
                        showScreenshotHint = showScreenshotHint && !saved,
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
            (0 until count).chunked(2).forEach { row ->
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
internal fun RecoveryKeyFaceHeader(
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
            .background(LocalContentColor.current.copy(alpha = 0.14f))
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

private val PREVIEW_PHRASE = RecoveryPhrase(
    listOf("river", "apple", "orbit", "candle", "frozen", "maple", "tunnel", "quiet", "velvet", "harbor", "pixel", "summit"),
)

@PreviewLightDark
@Composable
private fun RecoveryKeyCardWordsPreview() {
    AppTheme {
        Surface {
            RecoveryKeyCard(
                phrase = PREVIEW_PHRASE,
                wordCount = Bip39WordCount.Twelve,
                showQrCode = false,
                onShowQrCodeSide = {},
                onShowWordsSide = {},
                saved = true,
                modifier = Modifier.padding(Dimens.spacingLarge),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun RecoveryKeyCardQrCodePreview() {
    AppTheme {
        Surface {
            RecoveryKeyCard(
                phrase = PREVIEW_PHRASE,
                wordCount = Bip39WordCount.Twelve,
                showQrCode = true,
                onShowQrCodeSide = {},
                onShowWordsSide = {},
                showScreenshotHint = true,
                modifier = Modifier.padding(Dimens.spacingLarge),
            )
        }
    }
}
