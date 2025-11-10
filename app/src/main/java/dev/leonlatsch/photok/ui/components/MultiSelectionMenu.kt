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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.ui.components.MultiSelectionState
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun MultiSelectionMenu(
    multiSelectionState: MultiSelectionState,
    modifier: Modifier = Modifier,
    actions: @Composable (ColumnScope.() -> Unit),
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = multiSelectionState.isActive.value,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
    ) {
        Card(
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Box {
                Row(
                    modifier = Modifier
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
                    IconButton(onClick = { multiSelectionState.showMore() }) {
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
                    RoundedDropdownMenu(
                        expanded = multiSelectionState.showMore.value,
                        onDismissRequest = {
                            multiSelectionState.dismissMore()
                        },
                        content = {
                            actions()
                        }
                    )
                }
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
                isActive.value = true
                selectedItems.value = listOf("", "")
                showMore.value = false
            },
            actions = {},
        )
    }
}
@Preview
@Composable
private fun MultiSelectionMenuPreviewOptions() {
    AppTheme {
        MultiSelectionMenu(
            multiSelectionState = MultiSelectionState(emptyList()).apply {
                isActive.value = true
                selectedItems.value = listOf("", "")
                showMore.value = true
            },
            actions = {},
        )
    }
}
