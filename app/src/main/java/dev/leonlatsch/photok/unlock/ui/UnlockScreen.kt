package dev.leonlatsch.photok.unlock.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.leonlatsch.photok.appstart.ui.AppStartState
import dev.leonlatsch.photok.core.R
import dev.leonlatsch.photok.ui.components.CenteredScrollableColumn
import dev.leonlatsch.photok.ui.components.PasswordField
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.uicomponents.AppName

@Composable
fun UnlockScreen() {
    AppTheme {
        UnlockScreenContent()
    }
}

@Composable
private fun UnlockScreenContent(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                AppName(
                    fontSize = 62.sp,
                    modifier = Modifier
                        .padding(top = 20.dp, bottom = 40.dp)
                )
            }
        },
        bottomBar = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                TextButton(
                    onClick = {
                        // TODO
                    },
                ) {
                    Text(stringResource(R.string.biometric_unlock_hint_button))
                }
            }
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(contentPadding)
        ) {
            Text(
                text = stringResource(R.string.unlock_title),
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.width(280.dp)
            )

            Spacer(Modifier.height(20.dp))

            var password by remember { mutableStateOf("") }
            val wrong by remember { mutableStateOf(false) }

            PasswordField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(R.string.unlock_enter_password),
                error = stringResource(R.string.unlock_wrong_password).takeIf { wrong },
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    // TODO
                },
                modifier = Modifier
                    .width(200.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(stringResource(R.string.unlock_button))
            }

            TextButton(
                onClick = {
                    // TODO
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(stringResource(R.string.recovery_phrase_forgot_password))
            }
        }
    }
}


@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    AppTheme {
        UnlockScreenContent()
    }
}
