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

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.leonlatsch.photok.main.ui.navigation.RootRoute
import dev.leonlatsch.photok.main.ui.navigation.TabNavigator
import dev.leonlatsch.photok.navigation.LocalNavigator
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingTopBar
import dev.leonlatsch.photok.onboarding.ui.components.step
import dev.leonlatsch.photok.onboarding.ui.screens.OnboardingBiometricUnlockScreen
import dev.leonlatsch.photok.onboarding.ui.screens.OnboardingRecoveryPhraseSetupScreen
import dev.leonlatsch.photok.onboarding.ui.screens.OnboardingSelectStartScreen
import dev.leonlatsch.photok.onboarding.ui.screens.OnboardingSetupPasswordScreen
import dev.leonlatsch.photok.onboarding.ui.screens.OnboardingWelcomeScreen
import dev.leonlatsch.photok.ui.ObserveAsEvents
import dev.leonlatsch.photok.ui.animation.slideBackward
import dev.leonlatsch.photok.ui.animation.slideForward

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel = hiltViewModel()) {
    val rootNavigator = LocalNavigator.current
    val activity = LocalActivity.current

    val backStack = rememberNavBackStack(*viewModel.initialBackStack.toTypedArray())
    val navigator = remember(backStack, rootNavigator) { TabNavigator(backStack, rootNavigator) }

    ObserveAsEvents(viewModel.navigationEvents) { event ->
        when (event) {
            OnboardingNavigationEvent.Finished -> rootNavigator.replaceAll(RootRoute.Main)
        }
    }

    BackHandler(enabled = backStack.size == 1) {
        activity?.finish()
    }

    val currentRoute = backStack.lastOrNull() as? OnboardingRoute
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

    CompositionLocalProvider(LocalNavigator provides navigator) {
        Box {
            NavDisplay(
                backStack = backStack,
                onBack = navigator::goBack,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                transitionSpec = { slideForward() },
                popTransitionSpec = { slideBackward() },
                predictivePopTransitionSpec = { slideBackward() },
                entryProvider = entryProvider {
                    entry<OnboardingRoute.Welcome> {
                        OnboardingWelcomeScreen()
                    }
                    entry<OnboardingRoute.SelectStart> {
                        OnboardingSelectStartScreen()
                    }
                    entry<OnboardingRoute.SetupPassword> {
                        OnboardingSetupPasswordScreen(
                            onVaultCreated = {
                                backStack.add(OnboardingRoute.BiometricUnlock)
                                backStack.removeAll { it != OnboardingRoute.BiometricUnlock }
                            },
                        )
                    }
                    entry<OnboardingRoute.BiometricUnlock> {
                        OnboardingBiometricUnlockScreen()
                    }
                    entry<OnboardingRoute.RecoveryPhraseSetup> {
                        OnboardingRecoveryPhraseSetupScreen(
                            onFinish = { viewModel.handleUiEvent(OnboardingUiEvent.Finish) },
                        )
                    }
                },
            )

            AnimatedVisibility(
                visible = step != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                OnboardingTopBar(step = step ?: lastStep)
            }
        }
    }
}
