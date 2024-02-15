/*
 *   Copyright 2020-2023 Leon Latsch
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

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.leonlatsch.photok.R

@Composable
fun MainMenu(
    uiState: MainMenuUiState,
    onNavigationItemClicked: (Int) -> Unit
) {
    NavigationBar(
        containerColor = colorResource(R.color.background)
    ) {
        MainNavItem(
            fragmentid = R.id.cgalleryFragment,
            currentSelectedFragmentId = uiState.currentFragmentId,
            iconRes = R.drawable.ic_image,
            label = stringResource(R.string.gallery_all_photos_label),
            onNavigationItemClicked = onNavigationItemClicked
        )

        MainNavItem(
            fragmentid = R.id.settingsFragment,
            currentSelectedFragmentId = uiState.currentFragmentId,
            iconRes = R.drawable.ic_settings,
            label = stringResource(R.string.menu_main_settings),
            onNavigationItemClicked = onNavigationItemClicked
        )
    }
}

@Preview
@Composable
private fun MainMenuPreview() {
    MaterialTheme {
        MainMenu(
            uiState = MainMenuUiState(R.id.cgalleryFragment),
            onNavigationItemClicked = {}
        )
    }
}


@Composable
private fun RowScope.MainNavItem(
    fragmentid: Int,
    currentSelectedFragmentId: Int,
    iconRes: Int,
    label: String,
    onNavigationItemClicked: (Int) -> Unit
) {
    val selectedIndicatorColor = colorResource(R.color.colorPrimaryDark).copy(alpha = .6f)
    val selectedColor = colorResource(R.color.textColor)
    val unselectedColor = colorResource(R.color.textColor)

    NavigationBarItem(
        selected = currentSelectedFragmentId == fragmentid,
        onClick = { onNavigationItemClicked(fragmentid) },
        icon = {
            Icon(painter = painterResource(iconRes), contentDescription = label)
        },
        label = {
            Text(label)
        },
        colors = NavigationBarItemDefaults.colors().copy(
            selectedIndicatorColor = selectedIndicatorColor,
            selectedIconColor = selectedColor,
            selectedTextColor = selectedColor,
            unselectedIconColor = unselectedColor,
            unselectedTextColor = unselectedColor
        ),
        alwaysShowLabel = true
    )
}