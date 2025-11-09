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

package dev.leonlatsch.photok.gallery.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.albums.ui.compose.AlbumItem
import dev.leonlatsch.photok.gallery.albums.ui.compose.CreateAlbumDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumPickerDialog(
    viewModel: AlbumPickerViewModel,
    onAlbumSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        AlbumPickerContent(
            uiState = uiState,
            onAlbumSelected = onAlbumSelected,
            onCreateNewAlbum = { showCreateDialog = true }
        )
    }


    CreateAlbumDialog(
        show = showCreateDialog,
        onDismissRequest = { showCreateDialog = false },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlbumPickerContent(
    uiState: AlbumPickerUiState,
    onAlbumSelected: (String) -> Unit,
    onCreateNewAlbum: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = {
                Text(
                    stringResource(R.string.gallery_albums_select_title),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
            ),
            actions = {
                IconButton(
                    onClick = { onCreateNewAlbum() }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = stringResource(R.string.magic_fab_new_album_label),
                    )
                }
            }
        )

        AlbumsGrid(
            albums = uiState.albums,
            onAlbumClicked = onAlbumSelected,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlbumPickerPreview() {
    AlbumPickerContent(
        uiState = AlbumPickerUiState(
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
        ),
        onAlbumSelected = {},
        onCreateNewAlbum = {},
    )
}