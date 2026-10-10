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

package dev.leonlatsch.photok.main.ui.navigation

import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.contains
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.metadata
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import dev.leonlatsch.photok.encryption.migration.ui.EncryptionMigrationScreen
import dev.leonlatsch.photok.encryption.ui.RecoveryPhraseRestoreScreen
import dev.leonlatsch.photok.main.ui.AppNavViewModel
import dev.leonlatsch.photok.navigation.LocalNavigator
import dev.leonlatsch.photok.onboarding.ui.OnboardingScreen
import dev.leonlatsch.photok.setup.ui.RecoveryPhraseSetupScreen
import dev.leonlatsch.photok.setup.ui.SetupScreen
import dev.leonlatsch.photok.ui.animation.slideBackward
import dev.leonlatsch.photok.ui.animation.slideForward
import dev.leonlatsch.photok.unlock.ui.UnlockScreen

private object NoTransitionWhenLeavingKey : NavMetadataKey<Boolean>

private val NoTransitionWhenLeaving = metadata { put(NoTransitionWhenLeavingKey, true) }

@Composable
fun AppNavHost(
    startTab: () -> MainTab,
    viewModel: AppNavViewModel = hiltViewModel(),
) {
    val startRoute by viewModel.startRoute.collectAsStateWithLifecycle()
    AppNavDisplay(
        startRoute = startRoute ?: return,
        startTab = startTab,
    )
}

@Composable
private fun AppNavDisplay(
    startRoute: RootRoute,
    startTab: () -> MainTab,
) {
    val backStack = rememberNavBackStack(startRoute)
    val navigator = remember(backStack) { RootNavigator(backStack) }

    LightStatusBarsEffect(backStack.lastOrNull())

    CompositionLocalProvider(LocalNavigator provides navigator) {
        NavDisplay(
            backStack = backStack,
            onBack = navigator::goBack,
            modifier = Modifier.fillMaxSize(),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            transitionSpec = { forwardTransition() },
            popTransitionSpec = { slideBackward() },
            predictivePopTransitionSpec = { slideBackward() },
            entryProvider = entryProvider {
                entry<RootRoute.Onboarding> {
                    OnboardingScreen()
                }
                entry<RootRoute.Setup> {
                    SetupScreen()
                }
                entry<RootRoute.Unlock> {
                    UnlockScreen()
                }
                entry<RootRoute.RecoveryPhraseSetup> {
                    RecoveryPhraseSetupScreen(onContinue = { navigator.replaceAll(RootRoute.Main) })
                }
                entry<RootRoute.RecoveryPhraseRestore> {
                    RecoveryPhraseRestoreScreen(onUnlocked = { navigator.replaceAll(RootRoute.Main) })
                }
                entry<RootRoute.EncryptionMigration>(metadata = NoTransitionWhenLeaving) {
                    EncryptionMigrationScreen(
                        onMigrationFinished = { navigator.replaceAll(RootRoute.Main) },
                    )
                }
                entry<RootRoute.Main> {
                    MainTabsScreen(homeTab = remember { startTab() })
                }
            },
        )
    }
}

@Composable
fun LightStatusBarsEffect(currentRoute: NavKey?) {
    val activity = LocalActivity.current
    val isNightMode = LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    LaunchedEffect(currentRoute, isNightMode) {
        val window = activity?.window ?: return@LaunchedEffect
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !isNightMode
    }
}

private fun AnimatedContentTransitionScope<Scene<NavKey>>.forwardTransition(): ContentTransform =
    if (NoTransitionWhenLeavingKey in initialState.metadata) {
        EnterTransition.None togetherWith ExitTransition.None
    } else {
        slideForward()
    }
