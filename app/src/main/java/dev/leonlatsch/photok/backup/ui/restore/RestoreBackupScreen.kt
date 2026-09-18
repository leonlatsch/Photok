package dev.leonlatsch.photok.backup.ui.restore

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.models.Algorithm
import dev.leonlatsch.photok.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreBackupScreen(
    backupUri: Uri,
    onBack: () -> Unit,
) {
    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("Restore Backup")
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack
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
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            tonalElevation = 10.dp,
                        ) {
                            Text(
                                text = "ZIP",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .padding(10.dp)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)
                        ) {
                            Text(
                                text = "photok_backup_2026_12_21.zip",
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.MiddleEllipsis,
                            )
                            Text(
                                text = "12. Sep 2026, 14:21 • 4,3 GB",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }

                    }

                }

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
                    StatCard(
                        label = "Photos",
                        icon = R.drawable.ic_image,
                        stat = "214",
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = "Videos",
                        icon = R.drawable.ic_videocam,
                        stat = "312",
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
                        stat = "7",
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = "Size",
                        icon = R.drawable.ic_database,
                        stat = "4,21GB",
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
                    value = "19. Sep 2026, 13:41 Uhr"
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                ArchiveInfoItem(
                    icon = R.drawable.ic_lock,
                    label = "Encryption",
                    value = Algorithm.AesCbcPkcs7Padding.value
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                ArchiveInfoItem(
                    icon = R.drawable.ic_check_circle,
                    label = "Backup format",
                    value = "Version 5"
                )
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
    RestoreBackupScreen(
        backupUri = Uri.EMPTY,
        onBack = {},
    )
}
