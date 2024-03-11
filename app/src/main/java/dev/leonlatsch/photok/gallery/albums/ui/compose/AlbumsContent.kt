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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.uicomponnets.compose.AppName

@Composable
fun AlbumsContent(content: AlbumsUiState.Content) {
    Box {
        AppName(
            color = Color.Black,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(WindowInsets.statusBars.asPaddingValues())
        )

        AlbumsGrid(
            albums = content.albums,
            extraPadding = 56.dp,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AlbumsGrid(albums: List<AlbumItem>, modifier: Modifier = Modifier, extraPadding: Dp) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(top = extraPadding),
        modifier = modifier
    ) {
        items(albums, key = { it.id }) { album ->
            AlbumPreviewTile(album)
        }
    }
}

@Composable
private fun AlbumPreviewTile(album: AlbumItem) {
    Card(modifier = Modifier.padding(12.dp)) {
        Box {
            val contentModifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)

            Box(
                modifier = contentModifier.background(Color.Red)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, colorResource(R.color.black_semi_transparent))
                        )
                    )
            )

            Text(
                text = album.name,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )

            Text(
                text = album.itemCount.toString(),
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AlbumsContentPreview() {
    AlbumsContent(
        content = AlbumsUiState.Content(
            listOf(
                AlbumItem(
                    id = "1",
                    name = "Album 1",
                    itemCount = 10
                ),
                AlbumItem(
                    id = "2",
                    name = "Album 2",
                    itemCount = 20
                ),
                AlbumItem(
                    id = "3",
                    name = "Album 3",
                    itemCount = 30
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
            )
        )
    )
}