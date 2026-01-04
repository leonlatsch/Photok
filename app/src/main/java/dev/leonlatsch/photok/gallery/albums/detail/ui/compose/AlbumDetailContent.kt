


package dev.leonlatsch.photok.gallery.albums.detail.ui.compose

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.albums.detail.ui.AlbumDetailUiEvent
import dev.leonlatsch.photok.gallery.albums.detail.ui.AlbumDetailUiState
import dev.leonlatsch.photok.gallery.components.PhotoGallery
import dev.leonlatsch.photok.gallery.components.PhotoTile
import dev.leonlatsch.photok.gallery.components.rememberMultiSelectionState
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun AlbumDetailContent(
    uiState: AlbumDetailUiState,
    handleUiEvent: (AlbumDetailUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val multiSelectionState =
        rememberMultiSelectionState(items = uiState.photos.map { it.uuid })

    PhotoGallery(
        photos = uiState.photos,
        albumName = uiState.albumName,
        multiSelectionState = multiSelectionState,
        onOpenPhoto = { handleUiEvent(AlbumDetailUiEvent.OpenPhoto(it)) },
        onExport = { targetUri ->
            handleUiEvent(
                AlbumDetailUiEvent.OnExport(
                    multiSelectionState.selectedItems.value.toList(),
                    targetUri,
                )
            )
        },
        onDelete = {
            handleUiEvent(
                AlbumDetailUiEvent.OnDelete(
                    multiSelectionState.selectedItems.value.toList()
                )
            )
        },
        onImportChoice = {
            handleUiEvent(AlbumDetailUiEvent.OnImportChoice(it))
        },
        additionalMultiSelectionActions = {
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_ms_remove_from_album)) },
                onClick = {
                    handleUiEvent(AlbumDetailUiEvent.RemoveFromAlbum(multiSelectionState.selectedItems.value.toList()))
                    multiSelectionState.dismissMore()
                    multiSelectionState.cancelSelection()
                },
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.menu_ms_remove_from_album),
                    )
                }
            )
        },
        modifier = modifier,
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AlbumsDetailScreenPreview() {
    AppTheme {
        AlbumDetailContent(
            uiState = AlbumDetailUiState(
                "",
                "Album Name",
                listOf(
                    PhotoTile("file1", PhotoType.JPEG, "uuid1"),
                    PhotoTile("file2", PhotoType.JPEG, "uuid2"),
                    PhotoTile("file3", PhotoType.JPEG, "uuid3"),
                    PhotoTile("file4", PhotoType.JPEG, "uuid4"),
                    PhotoTile("file5", PhotoType.JPEG, "uuid5"),
                    PhotoTile("file6", PhotoType.JPEG, "uuid6"),
                    PhotoTile("file7", PhotoType.JPEG, "uuid7"),
                    PhotoTile("file8", PhotoType.JPEG, "uuid8"),
                )
            ),
            handleUiEvent = {},
        )
    }
}


package dev.leonlatsch.photok.gallery.albums.detail.ui.compose

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.albums.detail.ui.AlbumDetailUiEvent
import dev.leonlatsch.photok.gallery.albums.detail.ui.AlbumDetailUiState
import dev.leonlatsch.photok.gallery.components.PhotoGallery
import dev.leonlatsch.photok.gallery.components.PhotoTile
import dev.leonlatsch.photok.gallery.components.rememberMultiSelectionState
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun AlbumDetailContent(
    uiState: AlbumDetailUiState,
    handleUiEvent: (AlbumDetailUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val multiSelectionState =
        rememberMultiSelectionState(items = uiState.photos.map { it.uuid })

    PhotoGallery(
        photos = uiState.photos,
        albumName = uiState.albumName,
        multiSelectionState = multiSelectionState,
        onOpenPhoto = { handleUiEvent(AlbumDetailUiEvent.OpenPhoto(it)) },
        onExport = { targetUri ->
            handleUiEvent(
                AlbumDetailUiEvent.OnExport(
                    multiSelectionState.selectedItems.value.toList(),
                    targetUri,
                )
            )
        },
        onDelete = {
            handleUiEvent(
                AlbumDetailUiEvent.OnDelete(
                    multiSelectionState.selectedItems.value.toList()
                )
            )
        },
        onImportChoice = {
            handleUiEvent(AlbumDetailUiEvent.OnImportChoice(it))
        },
        additionalMultiSelectionActions = {
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_ms_remove_from_album)) },
                onClick = {
                    handleUiEvent(AlbumDetailUiEvent.RemoveFromAlbum(multiSelectionState.selectedItems.value.toList()))
                    multiSelectionState.dismissMore()
                    multiSelectionState.cancelSelection()
                },
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.menu_ms_remove_from_album),
                    )
                }
            )
        },
        modifier = modifier,
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AlbumsDetailScreenPreview() {
    AppTheme {
        AlbumDetailContent(
            uiState = AlbumDetailUiState(
                "",
                "Album Name",
                listOf(
                    PhotoTile("file1", PhotoType.JPEG, "uuid1"),
                    PhotoTile("file2", PhotoType.JPEG, "uuid2"),
                    PhotoTile("file3", PhotoType.JPEG, "uuid3"),
                    PhotoTile("file4", PhotoType.JPEG, "uuid4"),
                    PhotoTile("file5", PhotoType.JPEG, "uuid5"),
                    PhotoTile("file6", PhotoType.JPEG, "uuid6"),
                    PhotoTile("file7", PhotoType.JPEG, "uuid7"),
                    PhotoTile("file8", PhotoType.JPEG, "uuid8"),
                )
            ),
            handleUiEvent = {},
        )
    }
}
