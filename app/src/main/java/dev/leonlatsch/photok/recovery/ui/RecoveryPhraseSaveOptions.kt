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
            Text("More options")
        }

        RoundedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Share as image") },
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
                text = { Text("Save as text file") },
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
                text = { Text("Copy to clipboard") },
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
                title = "Saved to Photos",
                description = "Look for it in Pictures/Photok. If your gallery backs up to the cloud, the image is uploaded too",
            )

            is RecoveryPhraseSavedTo.File -> SavedIndicatorRow(
                title = if (shown.fileName != null) "Saved to ${shown.fileName}" else "Saved as file",
                description = "Find it in the folder you picked",
            )

            is RecoveryPhraseSavedTo.Shared -> SavedIndicatorRow(
                title = if (shown.appName != null) "Shared with ${shown.appName}" else "Shared",
                description = "Make sure it arrived where you sent it",
            )

            RecoveryPhraseSavedTo.Clipboard -> SavedIndicatorRow(
                title = "Copied to clipboard",
                description = "Paste it somewhere safe outside this phone",
            )

            RecoveryPhraseSavedTo.Screenshot -> SavedIndicatorRow(
                title = "Screenshot taken",
                description = "Look for it in your Screenshots album",
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
