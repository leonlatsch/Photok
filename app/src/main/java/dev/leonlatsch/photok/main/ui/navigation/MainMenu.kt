


package dev.leonlatsch.photok.main.ui.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun MainMenu(
    uiState: MainMenuUiState,
    onNavigationItemClicked: (Int) -> Unit
) {
    NavigationBar(
        containerColor = colorResource(R.color.background)
    ) {
        MainNavItem(
            fragmentsId = R.id.galleryFragment,
            currentSelectedFragmentId = uiState.currentFragmentId,
            iconRes = R.drawable.ic_image,
            label = stringResource(R.string.gallery_all_photos_label),
            onNavigationItemClicked = onNavigationItemClicked
        )

        MainNavItem(
            fragmentsId = R.id.albumsFragment,
            additionalFragmentsId = listOf(R.id.albumDetailFragment),
            currentSelectedFragmentId = uiState.currentFragmentId,
            iconRes = R.drawable.ic_folder,
            label = stringResource(R.string.gallery_albums_label),
            onNavigationItemClicked = onNavigationItemClicked
        )

        MainNavItem(
            fragmentsId = R.id.settingsFragment,
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
    AppTheme {
        MainMenu(
            uiState = MainMenuUiState(R.id.galleryFragment),
            onNavigationItemClicked = {}
        )
    }
}


@Composable
private fun RowScope.MainNavItem(
    fragmentsId: Int,
    currentSelectedFragmentId: Int,
    iconRes: Int,
    label: String,
    onNavigationItemClicked: (Int) -> Unit,
    additionalFragmentsId: List<Int> = emptyList(),
) {

    NavigationBarItem(
        selected = currentSelectedFragmentId == fragmentsId || additionalFragmentsId.contains(
            currentSelectedFragmentId
        ),
        onClick = { onNavigationItemClicked(fragmentsId) },
        icon = {
            Icon(painter = painterResource(iconRes), contentDescription = label)
        },
        label = {
            Text(label)
        },
        alwaysShowLabel = true
    )
}

package dev.leonlatsch.photok.main.ui.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun MainMenu(
    uiState: MainMenuUiState,
    onNavigationItemClicked: (Int) -> Unit
) {
    NavigationBar(
        containerColor = colorResource(R.color.background)
    ) {
        MainNavItem(
            fragmentsId = R.id.galleryFragment,
            currentSelectedFragmentId = uiState.currentFragmentId,
            iconRes = R.drawable.ic_image,
            label = stringResource(R.string.gallery_all_photos_label),
            onNavigationItemClicked = onNavigationItemClicked
        )

        MainNavItem(
            fragmentsId = R.id.albumsFragment,
            additionalFragmentsId = listOf(R.id.albumDetailFragment),
            currentSelectedFragmentId = uiState.currentFragmentId,
            iconRes = R.drawable.ic_folder,
            label = stringResource(R.string.gallery_albums_label),
            onNavigationItemClicked = onNavigationItemClicked
        )

        MainNavItem(
            fragmentsId = R.id.settingsFragment,
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
    AppTheme {
        MainMenu(
            uiState = MainMenuUiState(R.id.galleryFragment),
            onNavigationItemClicked = {}
        )
    }
}


@Composable
private fun RowScope.MainNavItem(
    fragmentsId: Int,
    currentSelectedFragmentId: Int,
    iconRes: Int,
    label: String,
    onNavigationItemClicked: (Int) -> Unit,
    additionalFragmentsId: List<Int> = emptyList(),
) {

    NavigationBarItem(
        selected = currentSelectedFragmentId == fragmentsId || additionalFragmentsId.contains(
            currentSelectedFragmentId
        ),
        onClick = { onNavigationItemClicked(fragmentsId) },
        icon = {
            Icon(painter = painterResource(iconRes), contentDescription = label)
        },
        label = {
            Text(label)
        },
        alwaysShowLabel = true
    )
}