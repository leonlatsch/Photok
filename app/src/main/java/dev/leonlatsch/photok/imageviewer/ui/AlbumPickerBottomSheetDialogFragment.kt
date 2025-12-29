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

package dev.leonlatsch.photok.imageviewer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.gallery.albums.ui.compose.CreateAlbumDialog
import dev.leonlatsch.photok.gallery.components.AlbumPickerContent
import dev.leonlatsch.photok.gallery.components.AlbumPickerViewModel
import dev.leonlatsch.photok.ui.theme.AppTheme

/**
 * Bottom Sheet Dialog for selecting an album to add a photo to.
 *
 * @since 2.0.0
 * @author Contributor
 */
@AndroidEntryPoint
class AlbumPickerBottomSheetDialogFragment(
    private val onAlbumSelected: (String) -> Unit,
) : BottomSheetDialogFragment() {

    private val viewModel: AlbumPickerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                var showCreateDialog by remember { mutableStateOf(false) }

                AppTheme {
                    AlbumPickerContent(
                        uiState = uiState,
                        onAlbumSelected = { albumId ->
                            onAlbumSelected(albumId)
                            dismiss()
                        },
                        onCreateNewAlbum = { showCreateDialog = true }
                    )
                }

                CreateAlbumDialog(
                    show = showCreateDialog,
                    onDismissRequest = { showCreateDialog = false },
                )
            }
        }
    }
}

