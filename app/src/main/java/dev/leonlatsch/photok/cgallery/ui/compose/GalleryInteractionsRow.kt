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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R

@Composable
fun GalleryInteractionsRow(
    onClose: () -> Unit,
    onSelectAll: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(colorResource(R.color.background))
            .padding(horizontal = 12.dp)
    ) {
        IconButton(onClick = onClose) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.process_close),
                tint = colorResource(R.color.appTitleColor)
            )
        }
        TextButton(onClick = onSelectAll) {
            Text(
                text = stringResource(R.string.menu_ms_select_all),
                color = colorResource(R.color.appTitleColor)
            )
        }
        TextButton(onClick = onDelete) {
            Text(
                text = stringResource(R.string.common_delete),
                color = colorResource(R.color.appTitleColor)
            )
        }
        TextButton(onClick = onExport) {
            Text(
                text = stringResource(R.string.common_export),
                color = colorResource(R.color.appTitleColor)
            )
        }
    }
}

@Preview
@Composable
private fun GalleryInteractionsPreview() {
    MaterialTheme {
        GalleryInteractionsRow(
            onClose = {},
            onSelectAll = {},
            onDelete = {},
            onExport = {},
        )
    }
}