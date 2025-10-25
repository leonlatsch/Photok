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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.sort.domain.Sort
import dev.leonlatsch.photok.ui.theme.AppTheme


@Composable
fun SortingMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    sort: Sort,
    onSortChanged: (Sort) -> Unit,
    modifier: Modifier = Modifier,
) {

    DropdownMenu(
        modifier = modifier.clip(MaterialTheme.shapes.large),
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        shape = MaterialTheme.shapes.large,
    ) {
        for (field in Sort.Field.entries) {
            DropdownMenuItem(
                text = {
                    Text(field.label)
                },
                trailingIcon = {
                    if (sort.field == field) {
                        Icon(
                            painterResource(R.drawable.ic_check),
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
                    if (sort.order == order) {
                        Icon(
                            painterResource(R.drawable.ic_check),
                            contentDescription = "Selected",
                        )
                    }
                },
                onClick = { onSortChanged(sort.copy(order = order)) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun Preview() {
    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    actions = {
                        SortingMenu(
                            expanded = true,
                            onDismissRequest = {},
                            sort = Sort.Default,
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