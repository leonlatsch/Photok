package dev.leonlatsch.photok.backup.ui.restore

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.ui.theme.AppTheme

/** [index] is the 1-based position of the file in the restore, used as a stable key. */
data class RestoreLogEntry(
    val index: Int,
    val fileName: String,
)

const val RESTORE_LOG_ROWS = 3

private val ROW_HEIGHT = 22.dp
private val VERTICAL_PADDING = 12.dp

/** Alpha per row, counted from the newest entry at the bottom. */
private val ROW_ALPHA = listOf(1f, 0.45f, 0.2f)

private const val PULSE_DURATION_MILLIS = 900

/**
 * The [RESTORE_LOG_ROWS] files restored last, newest at the bottom, older ones fading out
 * to the top. Lines do not animate, they just appear — it is a log.
 */
@Composable
fun RestoreLog(
    entries: List<RestoreLogEntry>,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .height(ROW_HEIGHT * RESTORE_LOG_ROWS + VERTICAL_PADDING * 2)
                .padding(horizontal = 15.dp, vertical = VERTICAL_PADDING)
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            endY = size.height / 2,
                        ),
                        blendMode = BlendMode.DstIn,
                    )
                }
        ) {
            val rows = entries.takeLast(RESTORE_LOG_ROWS)

            rows.forEachIndexed { index, entry ->
                val newest = index == rows.lastIndex

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .height(ROW_HEIGHT)
                        .fillMaxWidth()
                        .alpha(ROW_ALPHA[rows.lastIndex - index])
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.width(6.dp)
                    ) {
                        if (newest) PulsingDot()
                    }

                    Text(
                        text = entry.fileName,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = if (newest) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        maxLines = 1,
                        overflow = TextOverflow.MiddleEllipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun PulsingDot() {
    val transition = rememberInfiniteTransition(label = "restoreLogDot")
    val pulse by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(PULSE_DURATION_MILLIS),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "restoreLogDotPulse",
    )

    Box(
        modifier = Modifier
            .size(6.dp)
            .alpha(pulse)
            .background(MaterialTheme.colorScheme.primary, CircleShape)
    )
}

@PreviewLightDark
@Composable
private fun Preview() {
    AppTheme {
        Surface {
            RestoreLog(
                entries = listOf(
                    RestoreLogEntry(40, "VID_20240418_101233.mp4"),
                    RestoreLogEntry(41, "IMG_20240418_102907.jpg"),
                    RestoreLogEntry(42, "IMG_20240418_112238.jpg"),
                ),
                modifier = Modifier.padding(20.dp),
            )
        }
    }
}
