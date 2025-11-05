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

package dev.leonlatsch.photok.gallery.sort.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateBounds
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.sort.domain.Sort
import dev.leonlatsch.photok.gallery.ui.GalleryUiState
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun SortingMenuIconButton(
    sort: Sort,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val filterChangedColor = MaterialTheme.colorScheme.tertiaryContainer

    val buttonContainerColor = remember(sort) {
        if (sort != Sort.Default) {
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
                contentDescription = "Sort",
            )
        }
    }
}


@Composable
fun SortingMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    sort: Sort,
    onSortChanged: (Sort) -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        shape = MaterialTheme.shapes.large,
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .animateContentSize()
    ) {
        for (field in Sort.Field.entries) {
            DropdownMenuItem(
                text = {
                    Text(field.label)
                },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = sort.field == field,
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
            DropdownMenuItem(
                text = {
                    Text(order.label)
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(order.icon),
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = sort.order == order,
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
                onClick = { onSortChanged(sort.copy(order = order)) },
            )
        }

        if (sort != Sort.Default) {
            DropdownMenuItem(
                text = {
                    TextButton(
                        onClick = { onSortChanged(Sort.Default) },
                        contentPadding = PaddingValues(),
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(text = "Restore Default")
                    }
                },
                onClick = { onSortChanged(Sort.Default) },
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
                            sort = Sort.Default.copy(field = Sort.Field.Size),
                            onClick = {}
                        )

                        SortingMenu(
                            expanded = true,
                            onDismissRequest = {},
                            sort = Sort.Default.copy(field = Sort.Field.Size),
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