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

package dev.leonlatsch.photok.main.ui.compose

import androidx.compose.material.BottomAppBar
import androidx.compose.material.Icon
import androidx.compose.material.NavigationRailItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import dev.leonlatsch.photok.R

@Preview
@Composable
fun MainMenu() {
    BottomAppBar(
        backgroundColor = colorResource(R.color.background),
    ) {
        NavigationRailItem(
            selected = true,
            onClick = { /*TODO*/ },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_image),
                    contentDescription = null
                )
            },
            label = { Text("All Photos") },
            selectedContentColor = colorResource(R.color.colorPrimary),
            unselectedContentColor = Color.DarkGray,
            alwaysShowLabel = true
        )
        NavigationRailItem(
            selected = false,
            onClick = { /*TODO*/ },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_video_library),
                    contentDescription = null
                )
            },
            label = {
                Text("Folders")
            },
            selectedContentColor = colorResource(R.color.colorPrimary),
            unselectedContentColor = Color.DarkGray,
            alwaysShowLabel = true
        )
        NavigationRailItem(
            selected = false,
            onClick = { /*TODO*/ },
            icon = {
                Icon(painter = painterResource(R.drawable.ic_settings), contentDescription = null)
            },
            label = {
                Text("Settings")
            },
            selectedContentColor = colorResource(R.color.colorPrimary),
            unselectedContentColor = Color.DarkGray,
            alwaysShowLabel = true
        )
    }
}