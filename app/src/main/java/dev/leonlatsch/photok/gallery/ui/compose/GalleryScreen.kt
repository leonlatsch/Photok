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

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.sort.ui.SortingMenu
import dev.leonlatsch.photok.gallery.ui.GalleryUiEvent
import dev.leonlatsch.photok.gallery.ui.GalleryUiState
import dev.leonlatsch.photok.gallery.ui.GalleryViewModel
import dev.leonlatsch.photok.gallery.ui.components.AlbumPickerDialog
import dev.leonlatsch.photok.gallery.ui.components.AlbumPickerViewModel
import dev.leonlatsch.photok.gallery.ui.components.rememberMultiSelectionState
import dev.leonlatsch.photok.ui.components.AppName
import dev.leonlatsch.photok.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    albumPickerViewModel: AlbumPickerViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    AppTheme {
        Scaffold(
            topBar = {
                LargeTopAppBar(
                    title = { AppName() },
                    windowInsets = WindowInsets.statusBars,
                    scrollBehavior = scrollBehavior,
                    actions = {
                        if (uiState is GalleryUiState.Content) {
                            var showSortMenu by remember { mutableStateOf(false) }

                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_close),
                                    contentDescription = "Sort",
                                )
                            }

                            SortingMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                sort = (uiState as GalleryUiState.Content).sort,
                                onSortChanged = { sort ->
                                    viewModel.handleUiEvent(GalleryUiEvent.SortChanged(sort))
                                }
                            )
                        }
                    }
                )
            },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { contentPadding ->
            val modifier = Modifier.padding(top = contentPadding.calculateTopPadding())

            when (uiState) {
                is GalleryUiState.Empty -> GalleryPlaceholder(
                    handleUiEvent = { viewModel.handleUiEvent(it) },
                    modifier = modifier,
                )

                is GalleryUiState.Content -> {
                    val contentUiState = uiState as GalleryUiState.Content
                    val multiSelectionState = rememberMultiSelectionState(
                        items = contentUiState.photos.map { it.uuid }
                    )

                    GalleryContent(
                        uiState = contentUiState,
                        handleUiEvent = { viewModel.handleUiEvent(it) },
                        multiSelectionState = multiSelectionState,
                        modifier = modifier,
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
}
