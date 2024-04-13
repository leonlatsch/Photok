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

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import dev.leonlatsch.photok.gallery.albums.ui.AlbumsUiEvent
import dev.leonlatsch.photok.gallery.albums.ui.AlbumsViewModel
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun AlbumsScreen(viewModel: AlbumsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    AppTheme {
        when (uiState) {
            is AlbumsUiState.Empty -> AlbumsPlaceholder(
                handleUiEvent = { viewModel.handleUiEvent(it) }

            )
            is AlbumsUiState.Content -> AlbumsContent(
                content = uiState as AlbumsUiState.Content,
                handleUiEvent = { viewModel.handleUiEvent(it)}
            )
        }

        CreateAlbumDialog(uiState = uiState, handleUiEvent = { viewModel.handleUiEvent(it) })
    }
}
