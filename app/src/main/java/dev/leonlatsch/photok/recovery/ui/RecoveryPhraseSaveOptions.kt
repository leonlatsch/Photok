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

package dev.leonlatsch.photok.recovery.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.components.RoundedDropdownMenu
import dev.leonlatsch.photok.ui.theme.Dimens

@Composable
fun MoreSaveOptionsMenu(
    onShareImage: () -> Unit,
    onSaveTextFile: () -> Unit,
    onCopyToClipboard: () -> Unit,
    modifier: Modifier = Modifier,
    wordCountItems: @Composable (closeMenu: () -> Unit) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        TextButton(
            onClick = { expanded = true }
        ) {
            Text(stringResource(R.string.recovery_phrase_more_options))
        }

        RoundedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.recovery_phrase_share_as_image)) },
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.ic_share),
                        contentDescription = null
                    )
                },
                onClick = {
                    expanded = false
                    onShareImage()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.recovery_phrase_save_as_text_file)) },
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.ic_download),
                        contentDescription = null
                    )
                },
                onClick = {
                    expanded = false
                    onSaveTextFile()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.recovery_phrase_copy_to_clipboard)) },
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.ic_content_copy),
                        contentDescription = null
                    )
                },
                onClick = {
                    expanded = false
                    onCopyToClipboard()
                },
            )

            HorizontalDivider()

            wordCountItems { expanded = false }
        }
    }
}

@Composable
fun RecoveryPhraseSavedIndicator(
    savedTo: RecoveryPhraseSavedTo?,
    modifier: Modifier = Modifier,
) {
    var lastSavedTo by remember { mutableStateOf(savedTo) }
    if (savedTo != null) lastSavedTo = savedTo

    AnimatedVisibility(
        visible = savedTo != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier,
    ) {
        when (val shown = lastSavedTo) {
            RecoveryPhraseSavedTo.Photos -> SavedIndicatorRow(
                title = stringResource(R.string.recovery_phrase_saved_photos_title),
                description = stringResource(R.string.recovery_phrase_saved_photos_description),
            )

            is RecoveryPhraseSavedTo.File -> SavedIndicatorRow(
                title = if (shown.fileName != null) stringResource(R.string.recovery_phrase_saved_to_file, shown.fileName) else stringResource(R.string.recovery_phrase_saved_as_file),
                description = stringResource(R.string.recovery_phrase_saved_file_description),
            )

            is RecoveryPhraseSavedTo.Shared -> SavedIndicatorRow(
                title = if (shown.appName != null) stringResource(R.string.recovery_phrase_shared_with, shown.appName) else stringResource(R.string.recovery_phrase_shared),
                description = stringResource(R.string.recovery_phrase_shared_description),
            )

            RecoveryPhraseSavedTo.Clipboard -> SavedIndicatorRow(
                title = stringResource(R.string.recovery_phrase_copied_title),
                description = stringResource(R.string.recovery_phrase_copied_description),
            )

            RecoveryPhraseSavedTo.Screenshot -> SavedIndicatorRow(
                title = stringResource(R.string.recovery_phrase_screenshot_title),
                description = stringResource(R.string.recovery_phrase_screenshot_description),
            )

            null -> Unit
        }
    }
}

@Composable
private fun SavedIndicatorRow(
    title: String,
    description: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colorScheme.secondaryContainer)
            .padding(Dimens.spacingMedium),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_check_circle),
            contentDescription = null,
            tint = colorScheme.onPrimaryContainer,
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colorScheme.onPrimaryContainer,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onPrimaryContainer,
            )
        }
    }
}
