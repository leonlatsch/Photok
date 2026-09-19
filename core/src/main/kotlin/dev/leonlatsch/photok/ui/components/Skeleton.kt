package dev.leonlatsch.photok.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import dev.leonlatsch.photok.ui.theme.AppTheme

private const val SHIMMER_DURATION_MILLIS = 1400
private const val SHIMMER_BAND_FRACTION = 0.7f

/**
 * Placeholder block for content that is still loading. Give it a size via [modifier].
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
) {
    Box(modifier = modifier.skeletonShimmer(shape))
}

/**
 * Draws an animated shimmer behind the content, clipped to [shape].
 * Use it directly when a whole container should read as loading.
 */
@Composable
fun Modifier.skeletonShimmer(shape: Shape = RoundedCornerShape(18.dp)): Modifier {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(SHIMMER_DURATION_MILLIS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "skeletonProgress",
    )

    val baseColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val highlightColor = lerp(baseColor, MaterialTheme.colorScheme.onSurface, 0.08f)

    return this
        .clip(shape)
        .drawBehind {
            val bandWidth = size.width * SHIMMER_BAND_FRACTION
            val startX = -bandWidth + progress.value * (size.width + bandWidth)

            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(baseColor, highlightColor, baseColor),
                    startX = startX,
                    endX = startX + bandWidth,
                )
            )
        }
}

@PreviewLightDark
@Composable
private fun Preview() {
    AppTheme {
        Surface {
            Column(
                verticalArrangement = Arrangement.spacedBy(15.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                )
                SkeletonBox(
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(16.dp)
                )
            }
        }
    }
}
