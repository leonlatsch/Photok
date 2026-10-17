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

package dev.leonlatsch.photok.onboarding.ui

import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.leonlatsch.photok.main.ui.navigation.RootRoute
import dev.leonlatsch.photok.navigation.LocalNavigator
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingBrandHeader
import dev.leonlatsch.photok.onboarding.ui.components.step
import dev.leonlatsch.photok.onboarding.ui.screens.OnboardingAddFirstPhotosScreen
import dev.leonlatsch.photok.onboarding.ui.screens.OnboardingBiometricUnlockScreen
import dev.leonlatsch.photok.onboarding.ui.screens.OnboardingDoneScreen
import dev.leonlatsch.photok.onboarding.ui.screens.OnboardingRecoveryPhraseSetupScreen
import dev.leonlatsch.photok.onboarding.ui.screens.OnboardingSetupPasswordScreen
import dev.leonlatsch.photok.onboarding.ui.screens.OnboardingWelcomeScreen
import dev.leonlatsch.photok.pro.navigation.proOnboardingEntries
import dev.leonlatsch.photok.settings.ui.compose.LocalPreferencesValues
import dev.leonlatsch.photok.ui.ObserveAsEvents
import dev.leonlatsch.photok.ui.animation.slideForward
import dev.leonlatsch.photok.ui.systembars.StatusBarBackground

val OnboardingCardWindowInsets: WindowInsets
    @Composable
    get() = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.statusBars)

@Composable
fun OnboardingScreen() {
    val viewModel: OnboardingViewModel = hiltViewModel()
    val preferencesValues by viewModel.preferencesValues.collectAsStateWithLifecycle()

    val rootNavigator = LocalNavigator.current
    val activity = LocalActivity.current

    DisposableEffect(activity) {
        val previousOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            previousOrientation?.let { activity.requestedOrientation = it }
        }
    }

    val backStack = rememberNavBackStack(*viewModel.initialBackStack.toTypedArray())
    val navigator =
        remember(backStack, rootNavigator) { OnboardingNavigator(backStack, rootNavigator) }

    ObserveAsEvents(viewModel.navigationEvents) { event ->
        when (event) {
            OnboardingNavigationEvent.Finished -> rootNavigator.replaceAll(RootRoute.Main)
        }
    }

    val currentRoute = backStack.lastOrNull()
    LaunchedEffect(currentRoute) {
        if (currentRoute != null) {
            viewModel.handleUiEvent(OnboardingUiEvent.StepChanged(currentRoute))
        }
    }

    val step = currentRoute?.step()
    var lastStep by remember { mutableIntStateOf(1) }
    LaunchedEffect(step) {
        if (step != null) lastStep = step
    }

    if (step != null) {
        StatusBarBackground(colorScheme.primary)
    }

    CompositionLocalProvider(
        LocalNavigator provides navigator,
        LocalPreferencesValues provides preferencesValues,
    ) {
        val density = LocalDensity.current
        var brandHeaderHeight by remember { mutableStateOf(0.dp) }

        Box {
            Surface(
                color = colorScheme.primary,
                contentColor = colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    OnboardingBrandHeader(
                        step = step ?: lastStep,
                        modifier = Modifier
                            .onSizeChanged {
                                brandHeaderHeight = with(density) { it.height.toDp() }
                            }
                            .statusBarsPadding()
                    )
                }
            }

            val brandHeaderFraction by animateFloatAsState(
                targetValue = if (step == null) 0f else 1f,
                animationSpec = tween(delayMillis = 300),
            )

            val rounding by animateDpAsState(
                targetValue = if (step == null) 0.dp else 28.dp,
                animationSpec = tween(delayMillis = 300),
            )

            Surface(
                shape = RoundedCornerShape(
                    topStart = rounding,
                    topEnd = rounding,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp,
                ),
                modifier = Modifier
                    .padding(top = brandHeaderHeight * brandHeaderFraction)
            ) {
                NavDisplay(
                    backStack = backStack,
                    onBack = navigator::goBack,
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                    transitionSpec = { slideForward() },
                    entryProvider = entryProvider {
                        entry<OnboardingRoute.Welcome> {
                            OnboardingWelcomeScreen(
                                handleUiEvent = viewModel::handleUiEvent,
                            )
                        }
                        entry<OnboardingRoute.SetupPassword> {
                            OnboardingSetupPasswordScreen()
                        }
                        entry<OnboardingRoute.BiometricUnlock> {
                            OnboardingBiometricUnlockScreen(
                                handleUiEvent = viewModel::handleUiEvent,
                            )
                        }
                        entry<OnboardingRoute.RecoveryPhraseSetup> {
                            OnboardingRecoveryPhraseSetupScreen()
                        }
                        entry<OnboardingRoute.AddFirstPhotos> {
                            OnboardingAddFirstPhotosScreen()
                        }
                        entry<OnboardingRoute.Done> {
                            OnboardingDoneScreen(
                                handleUiEvent = viewModel::handleUiEvent,
                            )
                        }
                        proOnboardingEntries(
                            onContinue = { navigator.navigate(OnboardingRoute.Done) },
                        )
                    },
                )
            }
        }
    }
}
