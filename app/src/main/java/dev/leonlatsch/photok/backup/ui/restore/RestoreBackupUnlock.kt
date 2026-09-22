package dev.leonlatsch.photok.backup.ui.restore

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.components.PasswordField
import dev.leonlatsch.photok.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreBackupUnlock(
    uiState: RestoreBackupUiState.Unlock,
    handleUiEvent: (RestoreBackupUiEvent) -> Unit,
) {
    BackHandler {
        handleUiEvent(RestoreBackupUiEvent.BackToOverviewClicked)
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Unlock Backup")
                },
                navigationIcon = {
                    IconButton(
                        onClick = { handleUiEvent(RestoreBackupUiEvent.BackToOverviewClicked) }
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
            // Opaque, so the content scrolling underneath does not show through the bar
            Surface(color = MaterialTheme.colorScheme.background) {
                Button(
                    onClick = { handleUiEvent(RestoreBackupUiEvent.ConfirmPasswordClicked) },
                    enabled = uiState.password.isNotEmpty() && !uiState.unlocking,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.unlocking) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp),
                            )

                            Spacer(Modifier.size(10.dp))
                        }

                        Text(
                            text = "Unlock & Restore",
                        )
                    }
                }
            }
        }
    ) { contentPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            Surface(
                shape = RoundedCornerShape(18.dp),
                tonalElevation = 10.dp,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_lock),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(15.dp)
                        .size(32.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Enter Backup Password",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "The backup is encrypted and needs to be unlocked with the password that was used to create it.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline,
            )

            Spacer(Modifier.height(20.dp))

            PasswordField(
                value = uiState.password,
                onValueChange = { handleUiEvent(RestoreBackupUiEvent.PasswordChanged(it)) },
                label = "Password",
                error = stringResource(R.string.unlock_wrong_password).takeIf { uiState.wrongPassword },
                onDone = { handleUiEvent(RestoreBackupUiEvent.ConfirmPasswordClicked) },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "This password will only be used to unlock the backup and never be stored anywhere.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(horizontal = 20.dp)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    AppTheme {
        RestoreBackupUnlock(
            uiState = RestoreBackupUiState.Unlock(
                fileName = "photok_backup_1234.zip",
                password = "",
                unlocking = false,
                wrongPassword = false,
            ),
            handleUiEvent = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewWrongPassword() {
    AppTheme {
        RestoreBackupUnlock(
            uiState = RestoreBackupUiState.Unlock(
                fileName = "photok_backup_1234.zip",
                password = "secret",
                unlocking = false,
                wrongPassword = true,
            ),
            handleUiEvent = {},
        )
    }
}
