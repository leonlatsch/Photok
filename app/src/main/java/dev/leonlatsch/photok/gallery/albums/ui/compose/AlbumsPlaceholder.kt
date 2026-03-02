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

package dev.leonlatsch.photok.gallery.albums.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.albums.ui.AlbumsUiEvent
import dev.leonlatsch.photok.ui.components.MagicFab
import dev.leonlatsch.photok.ui.theme.AppTheme


@Composable
fun AlbumsPlaceholder(
    handleUiEvent: (AlbumsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.align(BiasAlignment(horizontalBias = 0f, verticalBias = -0.1f))
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_folder),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(92.dp).alpha(0.5f)
            )

            Text(
                text = stringResource(R.string.gallery_albums_placeholder),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
        }

        MagicFab(
            label = stringResource(R.string.magic_fab_new_album_label),
            onClick = {
                handleUiEvent(AlbumsUiEvent.ShowCreateDialog)
            },
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AlbumsPlaceholderPreview() {
    AppTheme {
        AlbumsPlaceholder(handleUiEvent = {})
    }
}