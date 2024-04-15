/*
 *   Copyright 2020-2024 Leon Latsch
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

package dev.leonlatsch.photok.gallery.albums.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.leonlatsch.photok.gallery.albums.ui.compose.AlbumItem
import dev.leonlatsch.photok.gallery.ui.components.AlbumsGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumPickerDialog(
    albums: List<AlbumItem>,
    onAlbumSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Select an album",
                style = MaterialTheme.typography.headlineSmall
            )

            AlbumsGrid(albums = albums, onAlbumClicked = onAlbumSelected)
        }
    }
}

@Preview
@Composable
private fun AlbumPickerDialogPreview() {
    AlbumPickerDialog(
        albums = listOf(
            AlbumItem(
                id = "1",
                name = "Album 1",
                itemCount = 10,
            ),
            AlbumItem(
                id = "2",
                name = "Album 2",
                itemCount = 20,
            ),
            AlbumItem(
                id = "3",
                name = "Album 3",
                itemCount = 30,
            ),
            AlbumItem(
                id = "4",
                name = "Album 4",
                itemCount = 40
            ),
            AlbumItem(
                id = "5",
                name = "Album 5",
                itemCount = 50
            ),
        ),
        onAlbumSelected = {},
        onDismiss = {}
    )
}