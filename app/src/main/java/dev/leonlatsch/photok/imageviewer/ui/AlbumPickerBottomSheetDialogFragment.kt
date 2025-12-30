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

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.gallery.albums.ui.compose.CreateAlbumDialog
import dev.leonlatsch.photok.gallery.components.AlbumPickerContent
import dev.leonlatsch.photok.gallery.components.AlbumPickerEvent
import dev.leonlatsch.photok.gallery.components.AlbumPickerViewModel
import dev.leonlatsch.photok.ui.theme.AppTheme
import kotlinx.coroutines.flow.collectLatest

/**
 * Bottom Sheet Dialog for selecting an album to add photos to.
 * Uses the shared AlbumPickerViewModel for linking photos to albums.
 *
 * @since 2.0.0
 * @author Contributor
 */
@AndroidEntryPoint
class AlbumPickerBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private val viewModel: AlbumPickerViewModel by viewModels()

    private var photoUUIDs: List<String> = emptyList()
    private var onSuccess: (() -> Unit)? = null

    fun setPhotosToLink(uuids: List<String>, onLinkSuccess: () -> Unit): AlbumPickerBottomSheetDialogFragment {
        this.photoUUIDs = uuids
        this.onSuccess = onLinkSuccess
        return this
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext())
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                var showCreateDialog by remember { mutableStateOf(false) }

                // Observe events for success
                LaunchedEffect(Unit) {
                    viewModel.events.collectLatest { event ->
                        when (event) {
                            AlbumPickerEvent.LinkSuccess -> {
                                onSuccess?.invoke()
                                dismiss()
                            }
                        }
                    }
                }

                AppTheme {
                    Surface(
                        color = BottomSheetDefaults.ContainerColor,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        AlbumPickerContent(
                            uiState = uiState,
                            onAlbumSelected = { albumId ->
                                viewModel.linkPhotosToAlbum(photoUUIDs, albumId)
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
}

