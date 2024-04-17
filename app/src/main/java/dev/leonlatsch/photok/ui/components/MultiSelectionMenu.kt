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

package dev.leonlatsch.photok.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.ui.components.MultiSelectionState
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun MultiSelectionMenu(
    modifier: Modifier = Modifier,
    multiSelectionState: MultiSelectionState,
    actions: @Composable (ColumnScope.(closeMore: () -> Unit) -> Unit),
) {
    val showMoreDefault = LocalInspectionMode.current
    var showMore by remember { mutableStateOf(showMoreDefault) }

    Card(
        shape = CircleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Box {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { multiSelectionState.cancelSelection() }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.process_close),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = stringResource(
                        R.string.menu_ms_info,
                        multiSelectionState.selectedItems.value.size
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { showMore = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_vert),
                        contentDescription = stringResource(R.string.common_more),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .matchParentSize()
            ) {
                DropdownMenu(
                    expanded = showMore,
                    onDismissRequest = { showMore = false },
                    modifier = Modifier.clip(MaterialTheme.shapes.medium),
                    content = {
                        actions { showMore = false }
                    }
                )
            }
        }
    }
}

@Preview
@Composable
private fun MultiSelectionMenuPreview() {
    AppTheme {
        MultiSelectionMenu(
            multiSelectionState = MultiSelectionState(emptyList()).apply {
                selectedItems.value = listOf("", "")
            },
            actions = {}
        )
    }
}