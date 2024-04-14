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

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.components.MultiSelectionMenu

@Composable
fun GalleryMultiSelectionMenu(
    onClose: () -> Unit,
    onSelectAll: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    onAddToAlbum: () -> Unit = {},
    numOfSelected: Int,
    modifier: Modifier = Modifier
) {
    MultiSelectionMenu(onClose = onClose, numOfSelected = numOfSelected) { closeActions ->
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_select_all),
                    contentDescription = null
                )
            },
            text = { Text(stringResource(R.string.menu_ms_select_all)) },
            onClick = {
                onSelectAll()
                closeActions()
            },
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_folder),
                    contentDescription = null
                )
            },
            text = { Text("Add to album") },
            onClick = {
                onAddToAlbum()
                closeActions()
            },
        )
        HorizontalDivider()
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = null
                )
            },
            text = { Text(stringResource(R.string.common_delete)) },
            onClick = {
                onDelete()
                closeActions()
            },
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_export),
                    contentDescription = null
                )
            },
            text = { Text(stringResource(R.string.common_export)) },
            onClick = {
                onExport()
                closeActions()
            },
        )
    }
}