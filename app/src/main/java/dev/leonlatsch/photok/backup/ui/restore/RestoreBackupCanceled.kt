package dev.leonlatsch.photok.backup.ui.restore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.components.CenteredScrollableColumn
import dev.leonlatsch.photok.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreBackupCanceled(
    uiState: RestoreBackupUiState.Canceled,
    onDone: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Restore Canceled")
                },
            )
        },
        bottomBar = {
            Button(
                onClick = onDone,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        }
    ) { contentPadding ->
        CenteredScrollableColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(72.dp)
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Restore canceled",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Nothing from ${uiState.fileName} was added to your vault. " +
                    "All files that were already restored have been deleted again.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    AppTheme {
        RestoreBackupCanceled(
            uiState = RestoreBackupUiState.Canceled(
                fileName = "photok_backup_1234.zip",
            ),
            onDone = {},
        )
    }
}
