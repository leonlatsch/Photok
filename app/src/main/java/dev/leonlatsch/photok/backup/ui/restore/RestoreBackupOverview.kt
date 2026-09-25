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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.backup.data.BackupMetaData
import dev.leonlatsch.photok.backup.domain.BackupValidation
import dev.leonlatsch.photok.databinding.BindingConverters
import dev.leonlatsch.photok.encryption.domain.models.Algorithm
import dev.leonlatsch.photok.encryption.domain.models.Kdf
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionParams
import dev.leonlatsch.photok.ui.theme.AppTheme
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreBackupOverview(
    uiState: RestoreBackupUiState.Overview,
    handleUiEvent: (RestoreBackupUiEvent) -> Unit,
    onClose: () -> Unit
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
        bottomBar = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding()
            ) {
                if (uiState.validation.notEnoughSpace) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Text(
                            text = stringResource(
                                R.string.backup_restore_not_enough_space,
                                BindingConverters.formatByteSizeConverter(
                                    uiState.validation.requiredBytes
                                ),
                                BindingConverters.formatByteSizeConverter(
                                    uiState.validation.usableBytes
                                ),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Button(
                    onClick = { handleUiEvent(RestoreBackupUiEvent.UnlockBackupClicked) },
                    enabled = !uiState.validation.notEnoughSpace,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(
                            5.dp,
                            Alignment.CenterHorizontally
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_lock),
                            contentDescription = null,
                        )

                        Text(stringResource(R.string.backup_restore_unlock_title))
                    }
                }
            }
        }
    ) { contentPadding ->

        val dateFormat = remember {
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
        }

        val formattedCreatedAt = remember(uiState.validation.metaData.createdAt) {
            dateFormat.format(uiState.validation.metaData.createdAt)
        }

        val formattedFileSize = remember(uiState.validation.fileSize) {
            BindingConverters.formatByteSizeConverter(uiState.validation.fileSize)
        }

        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            BackupFileHeader(
                fileName = uiState.validation.fileName,
                subtitle = "$formattedCreatedAt • $formattedFileSize",
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
                val photos = uiState.validation.metaData.photos

                val photoCount = remember(photos) { photos.count { !it.type.isVideo } }
                val videoCount = remember(photos) { photos.count { it.type.isVideo } }

                StatCard(
                    label = stringResource(R.string.backup_restore_stats_photos),
                    icon = R.drawable.ic_image,
                    stat = photoCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = stringResource(R.string.backup_restore_stats_videos),
                    icon = R.drawable.ic_videocam_outline,
                    stat = videoCount.toString(),
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(15.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                StatCard(
                    label = stringResource(R.string.backup_restore_stats_albums),
                    icon = R.drawable.ic_folder,
                    stat = uiState.validation.metaData.albums.size.toString(),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = stringResource(R.string.backup_restore_stats_size),
                    icon = R.drawable.ic_database,
                    stat = formattedFileSize,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.backup_restore_archive),
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(Modifier.height(5.dp))

            ArchiveInfoItem(
                icon = R.drawable.ic_schedule,
                label = stringResource(R.string.backup_restore_archive_created),
                value = formattedCreatedAt
            )
            HorizontalDivider(
                modifier = Modifier.padding(10.dp)
            )
            ArchiveInfoItem(
                icon = R.drawable.ic_lock,
                label = stringResource(R.string.backup_restore_archive_encryption),
                value = when (val metadate = uiState.validation.metaData) {
                    is BackupMetaData.V1 -> Algorithm.AesGcmNoPadding
                    is BackupMetaData.V2 -> Algorithm.AesGcmNoPadding
                    is BackupMetaData.V3 -> Algorithm.AesGcmNoPadding
                    is BackupMetaData.V4 -> Algorithm.AesCbcPkcs7Padding
                    is BackupMetaData.V5 -> metadate.params.algorithm
                }.value
            )
            HorizontalDivider(
                modifier = Modifier.padding(10.dp)
            )

            val backupVersion = uiState.validation.metaData.backupVersion

            val subtitle = if (backupVersion == BackupMetaData.CURRENT_BACKUP_VERSION) {
                stringResource(R.string.backup_restore_compatibility_up_to_date, backupVersion)
            } else {
                stringResource(R.string.backup_restore_compatibility_backwards, backupVersion)
            }

            ArchiveInfoItem(
                icon = R.drawable.ic_extension,
                label = stringResource(R.string.backup_restore_archive_compatibility),
                value = subtitle,
            )

            if (!uiState.emptyVault) {
                Spacer(Modifier.height(30.dp))

                Text(
                    text = stringResource(R.string.backup_restore_duplicates),
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )

                Spacer(Modifier.height(5.dp))

                DuplicateHandlingOption(
                    label = stringResource(R.string.backup_restore_duplicates_skip),
                    description = stringResource(R.string.backup_restore_duplicates_skip_description),
                    selected = uiState.duplicateHandling == DuplicateHandling.Skip,
                    onClick = {
                        handleUiEvent(
                            RestoreBackupUiEvent.DuplicateHandlingChanged(DuplicateHandling.Skip)
                        )
                    },
                )

                DuplicateHandlingOption(
                    label = stringResource(R.string.backup_restore_duplicates_replace),
                    description = stringResource(R.string.backup_restore_duplicates_replace_description),
                    selected = uiState.duplicateHandling == DuplicateHandling.Replace,
                    onClick = {
                        handleUiEvent(
                            RestoreBackupUiEvent.DuplicateHandlingChanged(DuplicateHandling.Replace)
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun DuplicateHandlingOption(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(10.dp),
    ) {
        RadioButton(selected = selected, onClick = null)

        Column {
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
fun ArchiveInfoItem(
    icon: Int,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.padding(horizontal = 10.dp),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        Column {
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = value,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    AppTheme {
        RestoreBackupOverview(
            uiState = RestoreBackupUiState.Overview(
                validation = BackupValidation(
                    metaData = BackupMetaData.V5(
                        photos = emptyList(),
                        albums = emptyList(),
                        albumPhotoRefs = emptyList(),
                        createdAt = Date().time,
                        backupVersion = 5,
                        wrappedVMK = "",
                        params = VaultProtectionParams(
                            salt = null,
                            iv = "",
                            kdf = Kdf.PBKDF2WithHmacSHA256,
                            kdfIterations = 100000,
                            algorithm = Algorithm.AesCbcPkcs7Padding,
                            keySize = 256,
                        )
                    ),
                    fileName = "photok_backup_1234.zip",
                    fileSize = 123123123L,
                    requiredBytes = 123123123L,
                    usableBytes = 999999999L,
                ),
                emptyVault = false,
                duplicateHandling = DuplicateHandling.Skip,
            ),
            handleUiEvent = {},
            onClose = {},
        )
    }
}