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

package dev.leonlatsch.photok.gallery.albums.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.albums.ui.AlbumsUiEvent
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun CreateAlbumDialog(uiState: AlbumsUiState, handleUiEvent: (AlbumsUiEvent) -> Unit) {
    if (uiState.showCreateDialog) {
        Dialog(onDismissRequest = { handleUiEvent(AlbumsUiEvent.HideCreateDialog) }) {
            Card {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    var albumName by remember { mutableStateOf("") }

                    Text("Create Album", style = MaterialTheme.typography.headlineSmall)

                    OutlinedTextField(
                        value = albumName,
                        onValueChange = { albumName = it },
                        placeholder = { Text(stringResource(R.string.gallery_albums_create_placeholder)) },
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TextButton(onClick = { handleUiEvent(AlbumsUiEvent.HideCreateDialog) }) {
                            Text("Cancel")
                        }
                        Button(onClick = {
                            handleUiEvent(AlbumsUiEvent.CreateAlbum(albumName))
                            handleUiEvent(AlbumsUiEvent.HideCreateDialog)
                        }) {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CreateAlbumDialogPreview() {
    AppTheme {
        CreateAlbumDialog(uiState = AlbumsUiState.Empty(showCreateDialog = true), handleUiEvent = {})
    }
}