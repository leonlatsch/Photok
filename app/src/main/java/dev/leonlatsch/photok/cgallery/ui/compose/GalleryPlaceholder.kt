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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.cgallery.ui.GalleryUiEvent

@Composable
fun GalleryPlaceholder(handleUiEvent: (GalleryUiEvent) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.ic_vault_colored),
                contentDescription = stringResource(R.string.gallery_placeholder),
                modifier = Modifier.alpha(0.3f)
            )

            Text(stringResource(R.string.gallery_placeholder))
        }

        FloatingActionButton(
            onClick = { handleUiEvent(GalleryUiEvent.OpenImportMenu) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
private fun GalleryPlaceholderPreview() {
    MaterialTheme {
        GalleryPlaceholder {}
    }
}