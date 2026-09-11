package dev.leonlatsch.photok.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.core.R
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun HoldToConfirmButton(
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.common_hold_to_confirm),
    holdDuration: Duration = 2.seconds,
    containerColor: Color = MaterialTheme.colorScheme.error,
    contentColor: Color = MaterialTheme.colorScheme.onError,
    progressColor: Color = contentColor.copy(alpha = 0.24f),
) {
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val progress = remember { Animatable(0f) }
    var confirmed by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed, holdDuration) {
        if (isPressed) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)

            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = (holdDuration.inWholeMilliseconds * (1f - progress.value)).toInt(),
                    easing = LinearEasing,
                )
            )

            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
            confirmed = true
            onConfirmed()
        } else {
            if (!confirmed && progress.value > 0f) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
            }

            progress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 200, easing = LinearEasing)
            )
        }
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = ButtonDefaults.shape,
        modifier = modifier,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {},
                )
                .drawBehind {
                    drawRect(
                        color = progressColor,
                        size = size.copy(width = size.width * progress.value.coerceIn(0f, 1f)),
                    )
                }
                .padding(ButtonDefaults.ContentPadding)
        )
    }
}

@Preview
@Composable
private fun Preview() {
    Surface() {
        HoldToConfirmButton(
            onConfirmed = {},
            modifier = Modifier.padding(20.dp)
        )
    }
}
