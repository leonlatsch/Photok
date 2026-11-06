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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Modifier
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

val MediaFilter.icon: Int @DrawableRes get() = when (this) {
    MediaFilter.All -> R.drawable.ic_select_all
    MediaFilter.Photos -> R.drawable.ic_image
    MediaFilter.Videos -> R.drawable.ic_videocam_outline
}

val MediaFilter.label: Int @StringRes get() = when (this) {
    MediaFilter.All -> R.string.filter_all
    MediaFilter.Photos -> R.string.filter_photos
    MediaFilter.Videos -> R.string.filter_videos
}

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
        modifier = modifier,
    ) {
        for (option in MediaFilter.entries) {
            val selected = filter == option
            val fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal

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
                leadingIcon = {
                    Icon(
                        painter = painterResource(option.icon),
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
                onClick = {
                    onFilterChanged(option)
                    onDismissRequest()
                },
            )
        }
    }
}

@Composable
fun FilterNoResultsHint(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.filter_no_results),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(Dimens.spacingMedium),
    )
}

private val filterNoResultsHintContent: @Composable () -> Unit = { FilterNoResultsHint() }

fun filterEmptyHint(filter: MediaFilter): (@Composable () -> Unit)? =
    filterNoResultsHintContent.takeIf { filter != MediaFilter.All }

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
                            onClick = {},
                        )

                        FilterMenu(
                            expanded = true,
                            onDismissRequest = {},
                            filter = MediaFilter.Videos,
                            onFilterChanged = {},
                        )
                    }
                )
            }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
            ) {
                FilterNoResultsHint()
            }
        }
    }
}
