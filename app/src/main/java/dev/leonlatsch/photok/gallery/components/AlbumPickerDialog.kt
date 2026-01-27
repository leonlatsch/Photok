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

package dev.leonlatsch.photok.gallery.components

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.albums.ui.compose.AlbumItem
import dev.leonlatsch.photok.gallery.albums.ui.compose.CreateAlbumDialog
import dev.leonlatsch.photok.uicomponnets.Dialogs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumPickerDialog(
    visible: Boolean,
    selectedItemIds: List<String>,
    onDismissRequest: () -> Unit,
    onAlbumSelected: () -> Unit = {},
) {
    if (visible) {
        val viewModel: AlbumPickerViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        ModalBottomSheet(onDismissRequest = onDismissRequest) {
            AlbumPickerContent(
                selectedItemIds = selectedItemIds,
                uiState = uiState,
                handleUiEvent = viewModel::handleUiEvent,
                onDismissRequest = onDismissRequest,
                onAlbumSelected = onAlbumSelected,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlbumPickerContent(
    selectedItemIds: List<String>,
    uiState: AlbumPickerUiState,
    handleUiEvent: (AlbumPickerUiEvent) -> Unit,
    onDismissRequest: () -> Unit,
    onAlbumSelected: () -> Unit,
) {
    var showCreateDialog by remember { mutableStateOf(false) }

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
                    onClick = { showCreateDialog = true }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = stringResource(R.string.magic_fab_new_album_label),
                    )
                }
            }
        )

        val context = LocalContext.current
        val addedMessage = stringResource(R.string.gallery_albums_photos_added, selectedItemIds.size)

        AlbumsGrid(
            albums = uiState.albums,
            onAlbumClicked = { uuid ->
                handleUiEvent(AlbumPickerUiEvent.OnAlbumSelected(selectedItemIds, uuid))
                Dialogs.showLongToast(context, addedMessage)
                onAlbumSelected()
                onDismissRequest()
            },
        )
    }

    CreateAlbumDialog(
        show = showCreateDialog,
        onDismissRequest = { showCreateDialog = false },
    )
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
        selectedItemIds = emptyList(),
        handleUiEvent = {},
        onDismissRequest = {},
        onAlbumSelected = {},
    )
}