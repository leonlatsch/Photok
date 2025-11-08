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

package dev.leonlatsch.photok.sort.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.sort.domain.Sort
import dev.leonlatsch.photok.sort.domain.SortConfig
import dev.leonlatsch.photok.ui.components.RoundedDropdownMenu
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun SortingMenuIconButton(
    sort: Sort,
    config: SortConfig,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val filterChangedColor = MaterialTheme.colorScheme.tertiaryContainer

    val buttonContainerColor = remember(sort) {
        if (sort != config.default) {
            filterChangedColor
        } else {
            Color.Unspecified
        }
    }

    Crossfade(
        targetState = buttonContainerColor,
        modifier = modifier,
    ) {
        IconButton(
            onClick = onClick,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = it,
            ),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_sort),
                contentDescription = stringResource(R.string.sorting_sort),
            )
        }
    }
}

@Composable
fun SortingMenu(
    config: SortConfig,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    sort: Sort,
    onSortChanged: (Sort) -> Unit,
    modifier: Modifier = Modifier,
) {
    RoundedDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.animateContentSize()
    ) {
        for (field in config.fields) {
            val selected = remember(sort.field) {
                sort.field == field
            }

            DropdownMenuItem(
                modifier = Modifier.semantics {
                    this.selected = selected
                },
                text = {
                    Text(stringResource(field.label))
                },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = selected,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = "Selected",
                        )
                    }
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(field.icon),
                        contentDescription = null,
                    )
                },
                onClick = { onSortChanged(sort.copy(field = field)) },
            )
        }

        HorizontalDivider()

        for (order in Sort.Order.entries) {
            val selected = remember(sort.order) {
                sort.order == order
            }

            DropdownMenuItem(
                modifier = Modifier.semantics {
                    this.selected = selected
                },
                text = {
                    Text(stringResource(order.label))
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(order.icon),
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = selected,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                        )
                    }
                },
                onClick = { onSortChanged(sort.copy(order = order)) },
            )
        }

        if (sort != config.default) {
            DropdownMenuItem(
                text = {
                    TextButton(
                        onClick = { onSortChanged(config.default) },
                        contentPadding = PaddingValues(),
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(text = stringResource(R.string.sorting_restore_default))
                    }
                },
                onClick = { onSortChanged(config.default) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun Preview() {
    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    actions = {
                        SortingMenuIconButton(
                            sort = SortConfig.Gallery.default.copy(field = Sort.Field.Size),
                            config = SortConfig.Gallery,
                            onClick = {}
                        )

                        SortingMenu(
                            config = SortConfig.Gallery,
                            expanded = true,
                            onDismissRequest = {},
                            sort = SortConfig.Gallery.default.copy(field = Sort.Field.Size),
                            onSortChanged = {}
                        )
                    }
                )
            }
        ) {
            Box(
                modifier = Modifier.padding(it)
            )
        }
    }
}