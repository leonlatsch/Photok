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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.backup.ui.restore.RestoreBackupScreen
import dev.leonlatsch.photok.devsettings.ui.compose.DevSettingsScreen
import dev.leonlatsch.photok.gallery.albums.detail.ui.compose.AlbumDetailScreen
import dev.leonlatsch.photok.gallery.albums.ui.compose.AlbumsScreen
import dev.leonlatsch.photok.gallery.ui.compose.GalleryScreen
import dev.leonlatsch.photok.imageviewer.ui.compose.ImageViewerScreen
import dev.leonlatsch.photok.navigation.LocalNavigator
import dev.leonlatsch.photok.pro.navigation.proEntries
import dev.leonlatsch.photok.settings.ui.AboutScreen
import dev.leonlatsch.photok.settings.ui.compose.SettingsScreen
import dev.leonlatsch.photok.settings.ui.credits.CreditsScreen
import dev.leonlatsch.photok.settings.ui.thirdparty.OssLicensesScreen
import dev.leonlatsch.photok.setup.ui.RecoveryPhraseSetupScreen
import dev.leonlatsch.photok.ui.animation.slideBackward
import dev.leonlatsch.photok.ui.animation.slideForward

private val RoutesWithMenu = listOfNotNull(
    AppRoute.Gallery,
    AppRoute.Albums,
    AppRoute.Settings,
    AppRoute.DevSettings.takeIf { BuildConfig.DEBUG },
)

@Composable
fun MainTabsScreen(homeTab: MainTab) {
    val rootNavigator = LocalNavigator.current

    var selectedTab by rememberSaveable { mutableStateOf(homeTab) }

    val galleryBackStack = rememberNavBackStack(MainTab.Gallery.rootRoute)
    val albumsBackStack = rememberNavBackStack(MainTab.Albums.rootRoute)
    val settingsBackStack = rememberNavBackStack(MainTab.Settings.rootRoute)

    val galleryEntries = rememberDecoratedNavEntries(
        backStack = galleryBackStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = { key -> tabEntry(MainTab.Gallery, key) },
    )
    val albumsEntries = rememberDecoratedNavEntries(
        backStack = albumsBackStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = { key -> tabEntry(MainTab.Albums, key) },
    )
    val settingsEntries = rememberDecoratedNavEntries(
        backStack = settingsBackStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = { key -> tabEntry(MainTab.Settings, key) },
    )

    val selectedBackStack = when (selectedTab) {
        MainTab.Gallery -> galleryBackStack
        MainTab.Albums -> albumsBackStack
        MainTab.Settings -> settingsBackStack
    }
    val selectedEntries = when (selectedTab) {
        MainTab.Gallery -> galleryEntries
        MainTab.Albums -> albumsEntries
        MainTab.Settings -> settingsEntries
    }
    val tabNavigator = remember(selectedBackStack, rootNavigator) {
        TabNavigator(selectedBackStack, rootNavigator)
    }
    val currentRoute = selectedBackStack.lastOrNull()

    LightStatusBarsEffect(currentRoute)

    BackHandler(enabled = selectedTab != homeTab && selectedBackStack.size == 1) {
        selectedTab = homeTab
    }

    val density = LocalDensity.current
    var mainMenuHeight by remember { mutableStateOf(0.dp) }

    CompositionLocalProvider(
        LocalNavigator provides tabNavigator,
        LocalMainMenuPadding provides PaddingValues(bottom = mainMenuHeight),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            key(selectedTab) {
                NavDisplay(
                    entries = selectedEntries,
                    onBack = tabNavigator::goBack,
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = { slideForward() },
                    popTransitionSpec = { slideBackward() },
                    predictivePopTransitionSpec = { slideBackward() },
                )
            }

            if (currentRoute in RoutesWithMenu || currentRoute is AppRoute.AlbumDetail) {
                MainMenu(
                    selectedTab = selectedTab,
                    onTabClicked = { tab ->
                        if (tab == selectedTab) {
                            popToRoot(selectedBackStack)
                        } else {
                            selectedTab = tab
                        }
                    },
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

private fun popToRoot(backStack: NavBackStack<NavKey>) {
    while (backStack.size > 1) {
        backStack.removeAt(backStack.lastIndex)
    }
}

private fun tabEntry(tab: MainTab, key: NavKey): NavEntry<NavKey> {
    val entry = tabEntryProvider(key)
    return NavEntry(
        key = key,
        contentKey = "${tab.name}:${entry.contentKey}",
        metadata = entry.metadata,
        content = { entry.Content() },
    )
}

private val tabEntryProvider = entryProvider<NavKey> {
    entry<AppRoute.Gallery> {
        GalleryScreen()
    }
    entry<AppRoute.Albums> {
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
    entry<AppRoute.Settings> {
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
    entry<AppRoute.RecoveryPhraseSetupFromSettings> {
        val navigator = LocalNavigator.current
        RecoveryPhraseSetupScreen(onContinue = navigator::goBack)
    }
    proEntries()
}
