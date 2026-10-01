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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
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
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.backup.ui.restore.RestoreBackupScreen
import dev.leonlatsch.photok.devsettings.ui.compose.DevSettingsScreen
import dev.leonlatsch.photok.encryption.migration.ui.EncryptionMigrationScreen
import dev.leonlatsch.photok.encryption.ui.RecoveryPhraseRestoreScreen
import dev.leonlatsch.photok.gallery.albums.detail.ui.compose.AlbumDetailScreen
import dev.leonlatsch.photok.gallery.albums.ui.compose.AlbumsScreen
import dev.leonlatsch.photok.gallery.ui.compose.GalleryScreen
import dev.leonlatsch.photok.imageviewer.ui.compose.ImageViewerScreen
import dev.leonlatsch.photok.main.ui.AppNavViewModel
import dev.leonlatsch.photok.navigation.LocalNavigator
import dev.leonlatsch.photok.pro.navigation.proEntries
import dev.leonlatsch.photok.settings.ui.AboutScreen
import dev.leonlatsch.photok.settings.ui.compose.SettingsScreen
import dev.leonlatsch.photok.settings.ui.credits.CreditsScreen
import dev.leonlatsch.photok.settings.ui.thirdparty.OssLicensesScreen
import dev.leonlatsch.photok.setup.ui.RecoveryPhraseSetupScreen
import dev.leonlatsch.photok.setup.ui.SetupScreen
import dev.leonlatsch.photok.ui.animation.slideBackward
import dev.leonlatsch.photok.ui.animation.slideForward
import dev.leonlatsch.photok.unlock.ui.UnlockScreen

private object TopLevelTabKey : NavMetadataKey<Boolean>
private object NoTransitionWhenLeavingKey : NavMetadataKey<Boolean>

private val TopLevelTab = metadata { put(TopLevelTabKey, true) }
private val NoTransitionWhenLeaving = metadata { put(NoTransitionWhenLeavingKey, true) }

private val RoutesWithMenu = listOfNotNull(
    AppRoute.Gallery,
    AppRoute.Albums,
    AppRoute.Settings,
    AppRoute.DevSettings.takeIf { BuildConfig.DEBUG },
)

@Composable
fun AppNavHost(
    startTab: () -> AppRoute,
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
    startRoute: AppRoute,
    startTab: () -> AppRoute,
) {
    val backStack = rememberNavBackStack(startRoute)
    val navigator = remember(backStack) { AppNavigator(backStack, startTab) }
    val currentRoute = backStack.lastOrNull()

    LightStatusBarsEffect(currentRoute)

    val density = LocalDensity.current
    var mainMenuHeight by remember { mutableStateOf(0.dp) }

    CompositionLocalProvider(
        LocalAppNavigator provides navigator,
        LocalNavigator provides navigator,
        LocalMainMenuPadding provides PaddingValues(bottom = mainMenuHeight),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavDisplay(
                backStack = backStack,
                onBack = navigator::goBack,
                modifier = Modifier.fillMaxSize(),
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                transitionSpec = { forwardTransition() },
                popTransitionSpec = { backwardTransition() },
                predictivePopTransitionSpec = { backwardTransition() },
                entryProvider = entryProvider {
                    entry<AppRoute.Setup> {
                        SetupScreen()
                    }
                    entry<AppRoute.Unlock> {
                        UnlockScreen()
                    }
                    entry<AppRoute.RecoveryPhraseSetup> {
                        RecoveryPhraseSetupScreen(onContinue = navigator::openStartTab)
                    }
                    entry<AppRoute.RecoveryPhraseSetupFromSettings> {
                        RecoveryPhraseSetupScreen(onContinue = navigator::goBack)
                    }
                    entry<AppRoute.RecoveryPhraseRestore> {
                        RecoveryPhraseRestoreScreen(onUnlocked = navigator::openStartTab)
                    }
                    entry<AppRoute.EncryptionMigration>(metadata = NoTransitionWhenLeaving) {
                        EncryptionMigrationScreen(
                            onMigrationFinished = { navigator.replaceAll(AppRoute.Gallery) },
                        )
                    }
                    entry<AppRoute.Gallery>(metadata = TopLevelTab) {
                        GalleryScreen()
                    }
                    entry<AppRoute.Albums>(metadata = TopLevelTab) {
                        AlbumsScreen()
                    }
                    entry<AppRoute.AlbumDetail> { route ->
                        AlbumDetailScreen(albumUuid = route.albumUuid)
                    }
                    entry<AppRoute.ImageViewer> { route ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black)
                        ) {
                            ImageViewerScreen(
                                photoUuid = route.photoUuid,
                                albumUuid = route.albumUuid,
                            )
                        }
                    }
                    entry<AppRoute.Settings>(metadata = TopLevelTab) {
                        SettingsScreen()
                    }
                    entry<AppRoute.About> {
                        AboutScreen()
                    }
                    entry<AppRoute.Credits> {
                        CreditsScreen()
                    }
                    entry<AppRoute.OssLicenses> {
                        OssLicensesScreen()
                    }
                    entry<AppRoute.DevSettings> {
                        DevSettingsScreen()
                    }
                    entry<AppRoute.RestoreBackup> { route ->
                        RestoreBackupScreen(backupUri = route.backupUri.toUri())
                    }
                    proEntries()
                },
            )

            if (currentRoute in RoutesWithMenu || currentRoute is AppRoute.AlbumDetail) {
                MainMenu(
                    currentRoute = currentRoute,
                    onTabClicked = navigator::selectTab,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .onSizeChanged { size ->
                            mainMenuHeight = with(density) { size.height.toDp() }
                        },
                )
            }
        }
    }
}

@Composable
private fun LightStatusBarsEffect(currentRoute: NavKey?) {
    val activity = LocalActivity.current
    val isNightMode = LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    LaunchedEffect(currentRoute, isNightMode) {
        val window = activity?.window ?: return@LaunchedEffect
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !isNightMode
    }
}

private fun AnimatedContentTransitionScope<Scene<NavKey>>.isTabSwitch(): Boolean =
    TopLevelTabKey in initialState.metadata && TopLevelTabKey in targetState.metadata

private fun AnimatedContentTransitionScope<Scene<NavKey>>.forwardTransition(): ContentTransform =
    if (isTabSwitch() || NoTransitionWhenLeavingKey in initialState.metadata) {
        noTransition()
    } else {
        slideForward()
    }

private fun AnimatedContentTransitionScope<Scene<NavKey>>.backwardTransition(): ContentTransform =
    if (isTabSwitch()) {
        noTransition()
    } else {
        slideBackward()
    }

private fun noTransition(): ContentTransform = EnterTransition.None togetherWith ExitTransition.None
