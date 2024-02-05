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

package dev.leonlatsch.photok.cgallery.ui.compose

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.leonlatsch.photok.cgallery.ui.GalleryUiState
import dev.leonlatsch.photok.cgallery.ui.GalleryViewModel

@Composable
fun GalleryScreen(viewModel: GalleryViewModel) {

    val uiState by viewModel.uiState.collectAsState()

    MaterialTheme {
        when (uiState) {
            is GalleryUiState.Empty -> GalleryPlaceholder { viewModel.handleUiEvent(it) }

            is GalleryUiState.Content -> GalleryContent(
                uiState as GalleryUiState.Content,
                handleUiEvent = { viewModel.handleUiEvent(it) }
            )
        }
    }
}
