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

package dev.leonlatsch.photok.gallery.albums.ui.compose

import androidx.compose.animation.Crossfade
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.albums.domain.DisplayMode
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun DisplayModeIconButton(
    displayMode: DisplayMode,
    onDisplayModeSelected: (DisplayMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Crossfade(displayMode) {
        when (it) {
            DisplayMode.Grid -> {
                IconButton(onClick = { onDisplayModeSelected(DisplayMode.List) }, modifier = modifier) {
                    Icon(
                        painter = painterResource(R.drawable.ic_list_view),
                        contentDescription = "Show albums as list",
                    )
                }
            }

            DisplayMode.List -> {
                IconButton(onClick = { onDisplayModeSelected(DisplayMode.Grid) }, modifier = modifier) {
                    Icon(
                        painter = painterResource(R.drawable.ic_grid_view),
                        contentDescription = "Show albums as grid",
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun DisplayModeIconButtonGridPreview() {
    AppTheme {
        DisplayModeIconButton(
            displayMode = DisplayMode.Grid,
            onDisplayModeSelected = {},
        )
    }
}

@Preview
@Composable
private fun DisplayModeIconButtonListPreview() {
    AppTheme {
        DisplayModeIconButton(
            displayMode = DisplayMode.List,
            onDisplayModeSelected = {},
        )
    }
}
