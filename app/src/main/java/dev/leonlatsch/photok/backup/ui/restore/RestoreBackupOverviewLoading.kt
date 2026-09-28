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

package dev.leonlatsch.photok.backup.ui.restore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.components.SkeletonBox
import dev.leonlatsch.photok.ui.theme.AppTheme

private val STAT_CARD_HEIGHT = 92.dp
private val ARCHIVE_SECTION_HEIGHT = 170.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreBackupOverviewLoading(
    uiState: RestoreBackupUiState.Validating,
    onClose: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.backup_restore_title))
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(20.dp)
        ) {
            BackupFileHeader(
                fileName = uiState.fileName,
                subtitle = stringResource(R.string.backup_restore_validating),
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.backup_restore_stats),
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(Modifier.height(5.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(STAT_CARD_HEIGHT)
                )
                SkeletonBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(STAT_CARD_HEIGHT)
                )
            }

            Spacer(Modifier.height(15.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(STAT_CARD_HEIGHT)
                )
                SkeletonBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(STAT_CARD_HEIGHT)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.backup_restore_archive),
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(Modifier.height(5.dp))

            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ARCHIVE_SECTION_HEIGHT)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    AppTheme {
        RestoreBackupOverviewLoading(
            uiState = RestoreBackupUiState.Validating(
                fileName = "photok_backup_1234.zip",
            ),
            onClose = {},
        )
    }
}
