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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.cgallery.ui.GalleryUiState
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.entity.PhotoType

@Composable
fun GalleryContent(uiState: GalleryUiState.Content) {
    Column {
        Text(text = stringResource(R.string.gallery_all_photos_label))

        LazyVerticalGrid(columns = GridCells.Fixed(4)) {
            items(uiState.photos, key = { it.uuid }) {
                GalleryPhotoTile(it)
            }
        }
    }
}

private val ImageSize = 25.dp

@Composable
private fun GalleryPhotoTile(photo: Photo) {
    ConstraintLayout(
        modifier = Modifier.padding(0.5.dp)
    ) {
        val (videoIconRef, imageRef, checkboxRef) = createRefs()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray)
                .constrainAs(imageRef) {
                    centerTo(parent)
                }
        )

        Image(
            painter = painterResource(R.drawable.ic_community),
            contentDescription = photo.fileName,
            modifier = Modifier
                .size(ImageSize)
                .constrainAs(imageRef) {
                    centerTo(parent)
                }
        )

        Icon(
            painter = painterResource(R.drawable.ic_videocam),
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier
                .constrainAs(videoIconRef) {
                    bottom.linkTo(parent.bottom, margin = 2.dp)
                    start.linkTo(parent.start, margin = 2.dp)
                }
        )

        var selected by remember { mutableStateOf(false) }

        Checkbox(
            checked = selected,
            onCheckedChange = { selected = !selected },
            modifier = Modifier.constrainAs(checkboxRef) {
                top.linkTo(parent.top, margin = 2.dp)
                start.linkTo(parent.start, margin = 2.dp)
            }
        )
    }

}

@Preview
@Composable
fun GalleryContentPreview() {
    GalleryContent(
        uiState = GalleryUiState.Content(
            listOf(
                Photo("", 0L, PhotoType.JPEG, 0L),
                Photo("", 0L, PhotoType.JPEG, 0L),
            )
        )
    )
}