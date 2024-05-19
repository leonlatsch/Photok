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

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.albums.ui.AlbumsViewModel
import dev.leonlatsch.photok.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(viewModel: AlbumsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    AppTheme {
        Scaffold(
            topBar = {
                LargeTopAppBar(
                    title = { Text(stringResource(R.string.gallery_albums_label)) },
                    scrollBehavior = scrollBehavior,
                )
            },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        ) { contentPadding ->
            val modifier = Modifier.padding(top = contentPadding.calculateTopPadding())

            when (uiState) {
                is AlbumsUiState.Empty -> AlbumsPlaceholder(
                    handleUiEvent = { viewModel.handleUiEvent(it) },
                    modifier = modifier,

                    )

                is AlbumsUiState.Content -> AlbumsContent(
                    content = uiState as AlbumsUiState.Content,
                    handleUiEvent = { viewModel.handleUiEvent(it) },
                    modifier = modifier,
                )
            }

            CreateAlbumDialog(uiState = uiState, handleUiEvent = { viewModel.handleUiEvent(it) })
        }
    }
}
