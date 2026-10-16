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

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FloatExponentialDecaySpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.navigation.LocalNavigator
import dev.leonlatsch.photok.onboarding.ui.OnboardingRoute
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingNexButtonBar
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.theme.Dimens
import dev.leonlatsch.photok.ui.uicomponents.AppName
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun OnboardingWelcomeScreen() {
    val navigator = LocalNavigator.current

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            OnboardingNexButtonBar(text = "Get started") {
                navigator.navigate(OnboardingRoute.SelectStart)
            }
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(contentPadding)
        ) {

            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.spacingLarge),
            ) {
                WelcomeMosaic()

                Spacer(Modifier.height(Dimens.spacingMedium))

                Column {
                    AppName(fontSize = 48.sp)
                    Text(
                        text = "A safe place for your photos",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }

                Spacer(Modifier.height(Dimens.spacingXSmall))

                FeatureRow(
                    icon = R.drawable.ic_lock,
                    text = "Encrypted on your phone with your password",
                )
                FeatureRow(
                    icon = R.drawable.ic_eye_closed,
                    text = "Hidden from your gallery and other apps",
                )
                FeatureRow(
                    icon = R.drawable.ic_cloud_off,
                    text = "No account, no cloud. Your data never leaves this phone",
                )
            }

        }
    }

}

private const val MOSAIC_EPSILON = 0.01f
private val MOSAIC_GAP = 10.dp
private val MOSAIC_TILE_SHAPE = RoundedCornerShape(20.dp)

private val MOSAIC_PHOTOS = listOf(
    R.drawable.onboarding_placeholder_photo_1,
    R.drawable.onboarding_placeholder_photo_2,
    R.drawable.onboarding_placeholder_photo_3,
    R.drawable.onboarding_placeholder_photo_4,
    R.drawable.onboarding_placeholder_photo_5,
    R.drawable.onboarding_placeholder_photo_6,
    R.drawable.onboarding_placeholder_photo_7,
    R.drawable.onboarding_placeholder_photo_8,
    R.drawable.onboarding_placeholder_photo_9,
    R.drawable.onboarding_placeholder_photo_10,
    R.drawable.onboarding_placeholder_photo_11,
    R.drawable.onboarding_placeholder_photo_12,
    R.drawable.onboarding_placeholder_photo_13,
    R.drawable.onboarding_placeholder_photo_14,
    R.drawable.onboarding_placeholder_photo_15,
    R.drawable.onboarding_placeholder_photo_16,
)

private class MosaicTileState(
    val height: Dp,
    @DrawableRes val photo: Int,
) {
    val extent: Float get() = height.value + MOSAIC_GAP.value
}

private fun randomMosaicTile() = MosaicTileState(
    height = listOf(72.dp, 88.dp, 104.dp, 120.dp).random(),
    photo = MOSAIC_PHOTOS.random(),
)

private class MosaicColumnState(private val viewportHeight: Float) {
    val tiles = mutableStateListOf<MosaicTileState>()
    var offset by mutableFloatStateOf(0f)
        private set
    var nextTopTile = randomMosaicTile()
        private set

    init {
        normalize()
    }

    fun scrollBy(delta: Float): Boolean {
        offset += delta
        return normalize()
    }

    suspend fun animateScrollBy(distance: Float, animationSpec: AnimationSpec<Float>) {
        var previous = 0f
        animate(initialValue = 0f, targetValue = distance, animationSpec = animationSpec) { value, _ ->
            scrollBy(value - previous)
            previous = value
        }
    }

    suspend fun settle(velocity: Float, onTileCrossed: () -> Unit) {
        var previous = 0f
        animateDecay(initialValue = 0f, initialVelocity = velocity, animationSpec = FloatExponentialDecaySpec()) { value, _ ->
            if (scrollBy(value - previous)) {
                onTileCrossed()
            }
            previous = value
        }
        val previousBoundary = -tiles.first().extent
        val target = if (offset < previousBoundary / 2) previousBoundary else 0f
        animateScrollBy(target - offset, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow))
    }

    private fun normalize(): Boolean {
        var tileCrossed = false
        while (offset > MOSAIC_EPSILON) {
            tiles.add(0, nextTopTile)
            offset -= nextTopTile.extent
            nextTopTile = randomMosaicTile()
            tileCrossed = true
        }
        while (tiles.isNotEmpty() && offset + tiles.first().extent <= MOSAIC_EPSILON) {
            offset += tiles.first().extent
            tiles.removeAt(0)
            tileCrossed = true
        }
        while (offset + tiles.sumOf { it.extent.toDouble() } < viewportHeight) {
            tiles.add(randomMosaicTile())
        }
        while (offset + tiles.sumOf { it.extent.toDouble() } - tiles.last().extent >= viewportHeight) {
            tiles.removeAt(tiles.lastIndex)
        }
        return tileCrossed
    }
}

@Composable
private fun WelcomeMosaic() {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val mosaicHeight = 260.dp + statusBarHeight
    val topFadeEnd = (statusBarHeight + 24.dp) / mosaicHeight

    Row(
        horizontalArrangement = Arrangement.spacedBy(MOSAIC_GAP),
        modifier = Modifier
            .fillMaxWidth()
            .height(mosaicHeight)
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.verticalGradient(
                        0f to Color.Transparent,
                        topFadeEnd to Color.Black,
                        0.85f to Color.Black,
                        1f to Color.Transparent,
                    ),
                    blendMode = BlendMode.DstIn,
                )
            },
    ) {
        MosaicColumnScrollingUp(height = mosaicHeight)
        MosaicColumnScrollingDown(height = mosaicHeight)
        MosaicColumnScrollingUp(height = mosaicHeight)
    }
}

@Composable
private fun RowScope.MosaicColumnScrollingUp(height: Dp) {
    val state = remember(height) { MosaicColumnState(viewportHeight = height.value) }
    var dragging by remember { mutableStateOf(false) }
    var releaseVelocity by remember { mutableFloatStateOf(0f) }
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(state, dragging) {
        if (dragging) return@LaunchedEffect
        state.settle(releaseVelocity) {
            haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
        }
        while (true) {
            delay(Random.nextLong(1200, 4000))
            state.animateScrollBy(
                distance = -state.tiles.first().extent,
                animationSpec = tween(Random.nextInt(900, 1500), easing = FastOutSlowInEasing),
            )
        }
    }

    MosaicColumn(
        state = state,
        onDragStarted = { dragging = true },
        onDragStopped = { velocity ->
            releaseVelocity = velocity
            dragging = false
        },
    )
}

@Composable
private fun RowScope.MosaicColumnScrollingDown(height: Dp) {
    val state = remember(height) { MosaicColumnState(viewportHeight = height.value) }
    var dragging by remember { mutableStateOf(false) }
    var releaseVelocity by remember { mutableFloatStateOf(0f) }
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(state, dragging) {
        if (dragging) return@LaunchedEffect
        state.settle(releaseVelocity) {
            haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
        }
        while (true) {
            delay(Random.nextLong(1200, 4000))
            state.animateScrollBy(
                distance = state.nextTopTile.extent,
                animationSpec = tween(Random.nextInt(900, 1500), easing = FastOutSlowInEasing),
            )
        }
    }

    MosaicColumn(
        state = state,
        onDragStarted = { dragging = true },
        onDragStopped = { velocity ->
            releaseVelocity = velocity
            dragging = false
        },
    )
}

@Composable
private fun RowScope.MosaicColumn(
    state: MosaicColumnState,
    onDragStarted: () -> Unit,
    onDragStopped: (velocity: Float) -> Unit,
) {
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clipToBounds()
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    if (state.scrollBy(with(density) { delta.toDp() }.value)) {
                        haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                    }
                },
                onDragStarted = { onDragStarted() },
                onDragStopped = { velocity ->
                    onDragStopped(with(density) { velocity.toDp() }.value)
                },
            ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MOSAIC_GAP),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.Top, unbounded = true)
                .offset { IntOffset(0, state.offset.dp.roundToPx()) },
        ) {
            state.tiles.forEach { tile ->
                key(tile) {
                    MosaicTile(tile = tile)
                }
            }
        }
    }
}

@Composable
private fun MosaicTile(tile: MosaicTileState) {
    Image(
        painter = painterResource(tile.photo),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .height(tile.height)
            .clip(MOSAIC_TILE_SHAPE),
    )
}

@Composable
private fun FeatureRow(
    @DrawableRes icon: Int,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
    ) {
        Surface(
            tonalElevation = 20.dp,
            shape = CircleShape,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier
                    .padding(10.dp)
                    .size(20.dp),
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
    }
}

@PreviewLightDark
@Composable
private fun OnboardingWelcomeScreenPreview() {
    AppTheme {
        OnboardingWelcomeScreen()
    }
}
