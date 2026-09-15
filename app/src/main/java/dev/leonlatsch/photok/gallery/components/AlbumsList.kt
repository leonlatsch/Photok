package dev.leonlatsch.photok.gallery.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.albums.ui.compose.AlbumItem
import dev.leonlatsch.photok.transcoding.compose.model.EncryptedImageRequestData
import dev.leonlatsch.photok.transcoding.compose.rememberEncryptedImagePainter
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun AlbumsList(
    albums: List<AlbumItem>,
    onAlbumClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)

    ) {
        items(albums, key = { it.id }) { album ->
            AlbumListItem(
                album = album,
                onAlbumClicked = onAlbumClicked,
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Composable
fun AlbumListItem(
    album: AlbumItem,
    onAlbumClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: @Composable () -> Unit = {},
) {
    val cornerRadius = 18.dp
    val contentPadding = 10.dp
    val imageCornerRadius = cornerRadius - contentPadding // Keep image concentric to the surface

    val shape = RoundedCornerShape(cornerRadius)

    Surface(
        tonalElevation = 6.dp,
        shape = shape,
        modifier = modifier
            .clip(shape)
            .clickable { onAlbumClicked(album.id) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(contentPadding),
            modifier = Modifier.padding(contentPadding)
        ) {
            val contentModifier = Modifier
                .width(80.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(imageCornerRadius))

            if (album.albumCover == null || LocalInspectionMode.current) {
                Box(
                    modifier = contentModifier.background(MaterialTheme.colorScheme.outline)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_folder),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(48.dp)
                    )
                }
            } else {
                val requestData = remember(album) {
                    EncryptedImageRequestData(
                        internalFileName = album.albumCover.filename,
                        mimeType = album.albumCover.mimeType
                    )
                }

                Image(
                    painter = rememberEncryptedImagePainter(requestData),
                    contentDescription = album.albumCover.filename,
                    modifier = contentModifier,
                    contentScale = ContentScale.Crop,
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = album.name,
                )

                val subtitle = if (album.itemCount == 0) {
                    stringResource(R.string.gallery_albums_empty_album)
                } else {
                    album.itemCount.toString()
                }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.outline,
                )
            }

            trailingContent()
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun AlbumsContentPreviewList() {
    AppTheme {
        AlbumsList(
            albums = listOf(
                AlbumItem(
                    id = "1",
                    name = "Album 1",
                    itemCount = 10,
                ),
                AlbumItem(
                    id = "2",
                    name = "Album 2",
                    itemCount = 20,
                ),
                AlbumItem(
                    id = "3",
                    name = "Album 3",
                    itemCount = 30,
                ),
                AlbumItem(
                    id = "4",
                    name = "Album 4",
                    itemCount = 40
                ),
                AlbumItem(
                    id = "5",
                    name = "Album 5",
                    itemCount = 50
                ),
            ),
            onAlbumClicked = {},
        )
    }
}
