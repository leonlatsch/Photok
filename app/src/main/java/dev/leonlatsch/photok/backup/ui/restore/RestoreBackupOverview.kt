package dev.leonlatsch.photok.backup.ui.restore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
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
    onClose: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Restore Backup")
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {},
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding()
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

                    Text("Unlock Backup")
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
                text = "Stats",
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(Modifier.height(5.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                val photoCount = remember(uiState.validation.metaData.photos.size) {
                    uiState.validation.metaData.photos.count { !it.type.isVideo }
                }
                val videoCount = remember(uiState.validation.metaData.photos.size) {
                    uiState.validation.metaData.photos.count { it.type.isVideo }
                }

                StatCard(
                    label = "Photos",
                    icon = R.drawable.ic_image,
                    stat = photoCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = "Videos",
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
                    label = "Albums",
                    icon = R.drawable.ic_folder,
                    stat = uiState.validation.metaData.albums.size.toString(),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = "Size",
                    icon = R.drawable.ic_database,
                    stat = formattedFileSize,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Archive",
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(Modifier.height(5.dp))

            ArchiveInfoItem(
                icon = R.drawable.ic_schedule,
                label = "Created",
                value = formattedCreatedAt
            )
            HorizontalDivider(
                modifier = Modifier.padding(10.dp)
            )
            ArchiveInfoItem(
                icon = R.drawable.ic_lock,
                label = "Encryption",
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

            val subtitle = remember {
                if (uiState.validation.metaData.backupVersion == BackupMetaData.CURRENT_BACKUP_VERSION) {
                    "Up to date (V${uiState.validation.metaData.backupVersion})"
                } else {
                    "Backwards compatible (V${uiState.validation.metaData.backupVersion})"
                }
            }

            ArchiveInfoItem(
                icon = R.drawable.ic_extension,
                label = "Compatibility",
                value = subtitle,
            )

            if (!uiState.emptyVault) {
                Spacer(Modifier.height(20.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text(
                        text = "You already have files in your Vault. Restoring merges them with the files from this backup.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    icon: Int,
    stat: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(18.dp),
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = label,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Text(
                text = stat,
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    maxFontSize = MaterialTheme.typography.headlineMedium.fontSize,
                )
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
                ),
                emptyVault = false,
            ),
            onClose = {},
        )
    }
}