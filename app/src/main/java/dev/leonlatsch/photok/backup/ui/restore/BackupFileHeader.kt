package dev.leonlatsch.photok.backup.ui.restore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun BackupFileHeader(
    fileName: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier,
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
                    text = fileName,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.MiddleEllipsis,
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    AppTheme {
        BackupFileHeader(
            fileName = "photok_backup_1234.zip",
            subtitle = "Sep 19, 2026, 10:30 • 117 MB",
        )
    }
}
