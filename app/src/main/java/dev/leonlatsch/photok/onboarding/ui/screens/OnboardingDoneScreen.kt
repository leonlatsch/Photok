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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.onboarding.ui.OnboardingUiEvent
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingNexButtonBar
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.theme.Dimens
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

private val VAULT_PHOTOS = listOf(
    R.drawable.onboarding_photo_1,
    R.drawable.onboarding_photo_2,
    R.drawable.onboarding_photo_3,
    R.drawable.onboarding_photo_4,
    R.drawable.onboarding_photo_5,
    R.drawable.onboarding_photo_6,
    R.drawable.onboarding_photo_7,
    R.drawable.onboarding_photo_8,
)
private val VAULT_PHOTO_TILTS = listOf(-12f, 8f, -6f, 14f, -10f, 6f, -14f, 10f)
private val VAULT_PHOTO_SIZE = 56.dp
private val VAULT_ORBIT_RADIUS = 115.dp
private val VAULT_SIZE = 300.dp

@Composable
fun OnboardingDoneScreen(
    handleUiEvent: (OnboardingUiEvent) -> Unit,
) {
    val inPreview = LocalInspectionMode.current
    var sealed by remember { mutableStateOf(inPreview) }

    val textAlpha by animateFloatAsState(
        targetValue = if (sealed) 1f else 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = 250),
    )
    val buttonAlpha by animateFloatAsState(
        targetValue = if (sealed) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 600),
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            OnboardingNexButtonBar(
                text = stringResource(R.string.onboarding_done_open_vault),
                enabled = sealed,
                onNext = {
                    handleUiEvent(OnboardingUiEvent.Finish)
                },
                modifier = Modifier.graphicsLayer { alpha = buttonAlpha },
            )
        },
    ) { contentPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium, Alignment.CenterVertically),
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.spacingLarge),
        ) {
            VaultSealAnimation(
                skipAnimation = inPreview,
                onSealed = { sealed = true },
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
                modifier = Modifier.graphicsLayer {
                    alpha = textAlpha
                    translationY = (1f - textAlpha) * 12.dp.toPx()
                },
            ) {
                Text(
                    text = stringResource(R.string.onboarding_done_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.onboarding_done_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer { alpha = 0.8f },
                )
            }
        }
    }
}

@Composable
private fun VaultSealAnimation(
    skipAnimation: Boolean,
    onSealed: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val startValue = if (skipAnimation) 1f else 0f

    val appear = remember { VAULT_PHOTOS.map { Animatable(startValue) } }
    val flight = remember { VAULT_PHOTOS.map { Animatable(startValue) } }
    var locked by remember { mutableStateOf(skipAnimation) }

    LaunchedEffect(Unit) {
        if (skipAnimation) return@LaunchedEffect

        delay(300.milliseconds)

        coroutineScope {
            appear.forEachIndexed { index, animatable ->
                launch {
                    delay((index * 50).milliseconds)
                    animatable.animateTo(1f, tween(durationMillis = 300))
                }
            }
        }

        delay(400.milliseconds)

        coroutineScope {
            flight.forEachIndexed { index, animatable ->
                launch {
                    delay((index * 90).milliseconds)
                    animatable.animateTo(1f, tween(durationMillis = 500, easing = FastOutSlowInEasing))
                }
            }
        }

        locked = true
        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
        onSealed()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(VAULT_SIZE),
    ) {
        VAULT_PHOTOS.forEachIndexed { index, photo ->
            VaultPhoto(
                photo = photo,
                index = index,
                appear = { appear[index].value },
                flight = { flight[index].value },
            )
        }

        val vaultColor by animateColorAsState(
            targetValue = if (locked) colorScheme.primary else colorScheme.primaryContainer,
            animationSpec = tween(durationMillis = 400),
        )
        val vaultContentColor by animateColorAsState(
            targetValue = if (locked) colorScheme.onPrimary else colorScheme.onPrimaryContainer,
            animationSpec = tween(durationMillis = 400),
        )

        Surface(
            shape = CircleShape,
            color = vaultColor,
            contentColor = vaultContentColor,
            modifier = Modifier.size(112.dp),
        ) {
            AnimatedContent(
                targetState = locked,
                transitionSpec = {
                    (fadeIn(tween(300, delayMillis = 100)) + scaleIn(tween(300, delayMillis = 100), initialScale = 0.7f)) togetherWith
                        (fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.7f))
                },
                contentAlignment = Alignment.Center,
                label = "VaultIcon",
            ) { shownLocked ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Icon(
                        painter = painterResource(if (shownLocked) R.drawable.ic_shield_check else R.drawable.ic_lock),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun VaultPhoto(
    @DrawableRes photo: Int,
    index: Int,
    appear: () -> Float,
    flight: () -> Float,
) {
    val angle = Math.toRadians(index * 360.0 / VAULT_PHOTOS.size - 90.0 + 22.5)

    Image(
        painter = painterResource(photo),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .graphicsLayer {
                val toVault = flight()
                val distance = VAULT_ORBIT_RADIUS.toPx() * (1f - toVault)
                translationX = (cos(angle) * distance).toFloat()
                translationY = (sin(angle) * distance).toFloat()
                val scale = 1f - 0.7f * toVault
                scaleX = scale
                scaleY = scale
                rotationZ = VAULT_PHOTO_TILTS[index] * (1f - toVault)
                alpha = appear().coerceIn(0f, 1f) * (1f - toVault * toVault)
            }
            .size(VAULT_PHOTO_SIZE)
            .clip(RoundedCornerShape(14.dp)),
    )
}

@PreviewLightDark
@Composable
private fun OnboardingDoneScreenPreview() {
    AppTheme {
        OnboardingDoneScreen(
            handleUiEvent = {}
        )
    }
}
