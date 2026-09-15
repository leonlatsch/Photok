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

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.albums.ui.compose.AlbumItem
import dev.leonlatsch.photok.transcoding.compose.model.EncryptedImageRequestData
import dev.leonlatsch.photok.transcoding.compose.rememberEncryptedImagePainter
import dev.leonlatsch.photok.ui.components.DialogViewModelStoreOwner
import dev.leonlatsch.photok.ui.theme.AppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeAlbumSheet(
    visible: Boolean,
    photoUuid: String,
    onDismissRequest: () -> Unit,
) {
    if (visible) {
        DialogViewModelStoreOwner {
            val viewModel = hiltViewModel<ChangeAlbumViewModel, ChangeAlbumViewModel.Factory>(
                creationCallback = { factory ->
                    factory.create(photoUuid)
                }
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { targetState ->
                    targetState != SheetValue.Hidden || (uiState as? ChangeAlbumUiState.Content)?.hasChanges == false
                }
            )
            val scope = rememberCoroutineScope()

            ModalBottomSheet(
                onDismissRequest = onDismissRequest,
                sheetState = sheetState,
            ) {
                ChangeAlbumContent(
                    uiState = uiState,
                    handleUiEvent = viewModel::handleUiEvent,
                    onSaveClicked = {
                        viewModel.handleUiEvent(ChangeAlbumUiEvent.Save)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            onDismissRequest()
                        }
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangeAlbumContent(
    uiState: ChangeAlbumUiState,
    handleUiEvent: (ChangeAlbumUiEvent) -> Unit,
    onSaveClicked: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        TopAppBar(
            title = {
                Text(
                    stringResource(R.string.change_album_title),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
            ),
            actions = {
                val saveEnabled = uiState is ChangeAlbumUiState.Content && uiState.hasChanges

                val saveIconColor by animateColorAsState(
                    if (saveEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )

                IconButton(
                    onClick = onSaveClicked,
                    enabled = saveEnabled,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = stringResource(R.string.common_save),
                        tint = saveIconColor,
                    )
                }
            }
        )

        when (uiState) {
            ChangeAlbumUiState.Loading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    CircularProgressIndicator()
                }
            }

            is ChangeAlbumUiState.Content -> {
                FileThumbnail(uiState.thumbnail)

                Spacer(Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.change_album_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )

                Spacer(Modifier.height(16.dp))

                if (uiState.albums.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    ) {
                        Text(
                            text = stringResource(R.string.gallery_albums_placeholder),
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 20.dp),
                    ) {
                        items(uiState.albums, key = { it.album.id }) { item ->
                            AlbumListItem(
                                album = item.album,
                                onAlbumClicked = {
                                    handleUiEvent(ChangeAlbumUiEvent.ToggleAlbum(it))
                                },
                                trailingContent = {
                                    Checkbox(
                                        checked = item.selected,
                                        onCheckedChange = {
                                            handleUiEvent(
                                                ChangeAlbumUiEvent.ToggleAlbum(item.album.id)
                                            )
                                        },
                                    )
                                },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FileThumbnail(
    thumbnail: EncryptedImageRequestData,
) {
    val modifier = Modifier
        .width(80.dp)
        .aspectRatio(1f)
        .clip(RoundedCornerShape(12.dp))

    if (LocalInspectionMode.current) {
        Box(modifier = modifier.background(MaterialTheme.colorScheme.outline))
    } else {
        Image(
            painter = rememberEncryptedImagePainter(thumbnail),
            contentDescription = thumbnail.internalFileName,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun ChangeAlbumPreview() {
    AppTheme {
        Surface(
            color = BottomSheetDefaults.ContainerColor
        ) {
            ChangeAlbumContent(
                uiState = ChangeAlbumUiState.Content(
                    thumbnail = EncryptedImageRequestData(
                        internalFileName = "preview",
                        mimeType = "image/jpeg",
                    ),
                    albums = listOf(
                        ChangeAlbumItem(
                            album = AlbumItem(id = "1", name = "Album 1", itemCount = 10),
                            selected = true,
                        ),
                        ChangeAlbumItem(
                            album = AlbumItem(id = "2", name = "Album 2", itemCount = 20),
                            selected = false,
                        ),
                        ChangeAlbumItem(
                            album = AlbumItem(id = "3", name = "Album 3", itemCount = 0),
                            selected = false,
                        ),
                    ),
                    hasChanges = true,
                ),
                handleUiEvent = {},
                onSaveClicked = {},
            )
        }
    }
}
