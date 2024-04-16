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

package dev.leonlatsch.photok.gallery.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.leonlatsch.photok.gallery.ui.GalleryUiEvent
import dev.leonlatsch.photok.gallery.ui.GalleryUiState
import dev.leonlatsch.photok.gallery.ui.GalleryViewModel
import dev.leonlatsch.photok.gallery.ui.components.AlbumPickerDialog
import dev.leonlatsch.photok.gallery.ui.components.AlbumPickerViewModel
import dev.leonlatsch.photok.gallery.ui.components.rememberMultiSelectionState
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    albumPickerViewModel: AlbumPickerViewModel,
) {

    val uiState by viewModel.uiState.collectAsState()

    AppTheme {
        when (uiState) {
            is GalleryUiState.Empty -> GalleryPlaceholder { viewModel.handleUiEvent(it) }

            is GalleryUiState.Content -> {
                val contentUiState = uiState as GalleryUiState.Content
                val multiSelectionState = rememberMultiSelectionState(
                    items = contentUiState.photos.map { it.uuid }
                )

                GalleryContent(
                    uiState = contentUiState,
                    handleUiEvent = { viewModel.handleUiEvent(it) },
                    multiSelectionState = multiSelectionState,
                )

                if (contentUiState.showAlbumSelectionDialog) {
                    AlbumPickerDialog(
                        viewModel = albumPickerViewModel,
                        onAlbumSelected = { selectedAlbum ->
                            viewModel.handleUiEvent(
                                GalleryUiEvent.OnAlbumSelected(
                                    multiSelectionState.selectedItems.value.toList(),
                                    selectedAlbum,
                                )
                            )
                            multiSelectionState.cancelSelection()
                        },
                        onDismiss = { viewModel.handleUiEvent(GalleryUiEvent.CancelAlbumSelection) }
                    )
                }
            }
        }
    }
}
