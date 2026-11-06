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

package dev.leonlatsch.photok.sort.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.sort.domain.MediaFilter
import dev.leonlatsch.photok.ui.components.RoundedDropdownMenu
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.theme.Dimens

@Composable
fun FilterMenuIconButton(
    filter: MediaFilter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val filterChangedColor = MaterialTheme.colorScheme.secondaryContainer

    val buttonContainerColor = remember(filter) {
        if (filter != MediaFilter.All) {
            filterChangedColor.copy(alpha = 0.5f)
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
                painter = painterResource(R.drawable.ic_filter),
                contentDescription = stringResource(R.string.filter_filter),
            )
        }
    }
}

@Composable
fun FilterMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    filter: MediaFilter,
    onFilterChanged: (MediaFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val itemColors = MenuDefaults.itemColors(
        trailingIconColor = MaterialTheme.colorScheme.primary,
    )

    RoundedDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.animateContentSize()
    ) {
        for (option in MediaFilter.entries) {
            val selected = remember(filter) {
                filter == option
            }
            val fontWeight = remember(selected) {
                if (selected) FontWeight.Bold else FontWeight.Normal
            }
            DropdownMenuItem(
                colors = itemColors,
                modifier = Modifier.semantics {
                    this.selected = selected
                },
                text = {
                    Text(
                        text = stringResource(option.label),
                        fontWeight = fontWeight,
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
                leadingIcon = {
                    Icon(
                        painter = painterResource(option.icon),
                        contentDescription = null,
                    )
                },
                onClick = {
                    onFilterChanged(option)
                    onDismissRequest()
                },
            )
        }
    }
}

@Composable
fun FilterPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .align(BiasAlignment(horizontalBias = 0f, verticalBias = -0.1f))
                .padding(horizontal = Dimens.spacingLarge)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_filter),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(92.dp).alpha(0.5f)
            )

            Text(
                stringResource(R.string.filter_placeholder),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
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
                        FilterMenuIconButton(
                            filter = MediaFilter.Videos,
                            onClick = {}
                        )

                        FilterMenu(
                            expanded = true,
                            onDismissRequest = {},
                            filter = MediaFilter.Videos,
                            onFilterChanged = {}
                        )
                    }
                )
            }
        ) {
            FilterPlaceholder(
                modifier = Modifier.padding(it)
            )
        }
    }
}
