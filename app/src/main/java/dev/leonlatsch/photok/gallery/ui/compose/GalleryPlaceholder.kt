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

package dev.leonlatsch.photok.gallery.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.ui.GalleryUiEvent
import dev.leonlatsch.photok.gallery.ui.components.MagicFabMenu
import dev.leonlatsch.photok.ui.components.MagicFab
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun GalleryPlaceholder(
    handleUiEvent: (GalleryUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {

        val magicFabMenuVisible = remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_vault_colored),
                contentDescription = stringResource(R.string.gallery_placeholder),
                modifier = Modifier.alpha(0.3f)
            )

            Text(
                stringResource(R.string.gallery_placeholder),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        AnimatedVisibility(
            visible = magicFabMenuVisible.value.not(),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            MagicFab {
                magicFabMenuVisible.value = true
            }
        }

        MagicFabMenu(openState = magicFabMenuVisible)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
private fun GalleryPlaceholderPreview() {
    AppTheme {
        GalleryPlaceholder(
            handleUiEvent = {}
        )
    }
}