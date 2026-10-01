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

package dev.leonlatsch.photok.main.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavKey
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.pro.intruderwarnings.rememberIntruderWarningCount
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun MainMenu(
    currentRoute: NavKey?,
    onTabClicked: (AppRoute) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        containerColor = colorResource(R.color.background),
        modifier = modifier,
    ) {
        MainNavItem(
            selected = currentRoute == AppRoute.Gallery,
            iconRes = R.drawable.ic_image,
            label = stringResource(R.string.gallery_all_photos_label),
            onClick = { onTabClicked(AppRoute.Gallery) },
        )

        MainNavItem(
            selected = currentRoute == AppRoute.Albums || currentRoute is AppRoute.AlbumDetail,
            iconRes = R.drawable.ic_folder,
            label = stringResource(R.string.gallery_albums_label),
            onClick = { onTabClicked(AppRoute.Albums) },
        )

        MainNavItem(
            selected = currentRoute == AppRoute.Settings,
            iconRes = R.drawable.ic_settings,
            label = stringResource(R.string.menu_main_settings),
            onClick = { onTabClicked(AppRoute.Settings) },
            badgeCount = rememberIntruderWarningCount()
        )
    }
}

@Preview
@Composable
private fun MainMenuPreview() {
    AppTheme {
        MainMenu(
            currentRoute = AppRoute.Gallery,
            onTabClicked = {}
        )
    }
}


@Composable
private fun RowScope.MainNavItem(
    selected: Boolean,
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
    badgeCount: Int = 0,
) {

    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Badge {
                            Text(badgeCount.toString())
                        }
                    }
                }
            ) {
                Icon(painter = painterResource(iconRes), contentDescription = label)
            }
        },
        label = {
            Text(label)
        },
        alwaysShowLabel = true
    )
}

val LocalMainMenuPadding = compositionLocalOf { PaddingValues() }
