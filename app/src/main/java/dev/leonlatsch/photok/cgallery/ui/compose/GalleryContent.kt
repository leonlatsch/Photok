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

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.cgallery.ui.GalleryUiEvent
import dev.leonlatsch.photok.cgallery.ui.GalleryUiState
import dev.leonlatsch.photok.cgallery.ui.MultiSelectionState
import dev.leonlatsch.photok.cgallery.ui.PhotoTile
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.uicomponnets.compose.AppName
import dev.leonlatsch.photok.uicomponnets.compose.findWindow
import java.util.UUID

private const val AnimationStiffness = Spring.StiffnessLow
private val FadeAnimationSpec: FiniteAnimationSpec<Float> = spring(stiffness = AnimationStiffness)

@Composable
fun GalleryContent(uiState: GalleryUiState.Content, handleUiEvent: (GalleryUiEvent) -> Unit) {
    val gridState = rememberLazyGridState()
    val window = findWindow()
    val isDarkTheme = isSystemInDarkTheme()

    Box {
        PhotosGrid(
            photos = uiState.photos,
            multiSelectionState = uiState.multiSelectionState,
            handleUiEvent = handleUiEvent,
            modifier = Modifier.fillMaxHeight(),
            extraTopPadding = 120.dp,
            gridState = gridState
        )

        val scrolling by remember { derivedStateOf { gridState.canScrollBackward } }

        AnimatedVisibility(
            visible = scrolling,
            enter = fadeIn(FadeAnimationSpec),
            exit = fadeOut(FadeAnimationSpec),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(colorResource(R.color.black_semi_transparent), Color.Transparent)
                        )
                    )
            )
        }

        LaunchedEffect(scrolling) {
            window?.let { window ->
                WindowCompat.getInsetsController(
                    window, window.decorView
                ).isAppearanceLightStatusBars = isDarkTheme.not() && scrolling.not()
            }
        }

        val titleColor by animateColorAsState(
            targetValue = if (scrolling) Color.White else colorResource(R.color.appTitleColor),
            animationSpec = spring(stiffness = AnimationStiffness),
            label = "titleColor"
        )

        AppName(
            color = titleColor,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(WindowInsets.statusBars.asPaddingValues())
        )

        AnimatedVisibility(
            visible = uiState.multiSelectionState.isActive.not(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
        ) {
            ImportButton(
                onClick = { handleUiEvent(GalleryUiEvent.OpenImportMenu) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
            )
        }

        AnimatedVisibility(
            visible = uiState.multiSelectionState.isActive,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
        ) {
            GalleryInteractionsRow(
                onClose = { handleUiEvent(GalleryUiEvent.CancelMultiSelect) },
                onSelectAll = { handleUiEvent(GalleryUiEvent.SelectAll) },
                onDelete = { handleUiEvent(GalleryUiEvent.OnDelete) },
                onExport = { handleUiEvent(GalleryUiEvent.OnExport) },
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF, showSystemUi = true)
@Composable
fun GalleryContentPreview() {
    MaterialTheme {
        GalleryContent(
            uiState = GalleryUiState.Content(
                selectionMode = true,
                listOf(
                    PhotoTile("", PhotoType.JPEG, UUID.randomUUID().toString()),
                    PhotoTile("", PhotoType.MP4, UUID.randomUUID().toString()),
                    PhotoTile("", PhotoType.GIF, UUID.randomUUID().toString()),
                    PhotoTile("", PhotoType.MPEG, "1"),
                    PhotoTile("", PhotoType.PNG, UUID.randomUUID().toString()),
                    PhotoTile("", PhotoType.PNG, UUID.randomUUID().toString()),
                    PhotoTile("", PhotoType.PNG, UUID.randomUUID().toString()),
                    PhotoTile("", PhotoType.PNG, UUID.randomUUID().toString()),
                    PhotoTile("", PhotoType.PNG, "2"),
                    PhotoTile("", PhotoType.PNG, UUID.randomUUID().toString()),
                    PhotoTile("", PhotoType.PNG, UUID.randomUUID().toString()),
                    PhotoTile("", PhotoType.PNG, UUID.randomUUID().toString()),
                    PhotoTile("", PhotoType.PNG, UUID.randomUUID().toString()),
                    PhotoTile("", PhotoType.PNG, UUID.randomUUID().toString()),
                    PhotoTile("", PhotoType.PNG, UUID.randomUUID().toString()),
                ),
                multiSelectionState = MultiSelectionState(
                    isActive = true,
                    selectedItemUUIDs = listOf("1", "2")
                )
            ),
            handleUiEvent = {},
        )
    }
}

