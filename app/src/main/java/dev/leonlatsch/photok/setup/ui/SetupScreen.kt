package dev.leonlatsch.photok.setup.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.leonlatsch.photok.R.string
import dev.leonlatsch.photok.core.R
import dev.leonlatsch.photok.ui.components.PasswordField
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.uicomponents.AppName

@Composable
fun SetupScreen(modifier: Modifier = Modifier) {
    AppTheme {
        SetupScreenContent()
    }
}

@Composable
private fun SetupScreenContent() {
    Scaffold(
        topBar = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 20.dp, bottom = 40.dp)
            ) {
                AppName(
                    fontSize = 62.sp,
                    modifier = Modifier
                )

                Text(
                    text = stringResource(R.string.setupSetup),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = stringResource(string.setup_create_your_password),
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.width(280.dp)
            )

            Spacer(Modifier.height(20.dp))

            PasswordField(
                value = "",
                onValueChange = {},
                label = stringResource(string.setup_enter_password),
                error = stringResource(string.setup_confirm_password).takeIf { false }, // does not match conditions
                onDone = {},
            )

            AnimatedVisibility(
                visible = true, // first password matches conditions
            ) {
                PasswordField(
                    value = "",
                    onValueChange = {},
                    label = stringResource(string.unlock_enter_password),
                    error = stringResource(string.unlock_wrong_password).takeIf { false }, // does not match conditions
                    onDone = {},
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            Spacer(Modifier.height(10.dp))


            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(horizontal = 15.dp)
            ) {
                Text(
                    text = stringResource(R.string.setup_password_strength_label),
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = stringResource(R.string.setup_password_strength_strong),// get real level from ui state and map to string
                    color = Color.Green // Same as for text. Get based on level
                )
            }

            AnimatedVisibility(
                visible = true, // passwords dont match
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = stringResource(R.string.setup_password_match_warning),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 20.dp)
                )
            }

            Button(
                onClick = {},
                enabled = true, // loading
                modifier = Modifier
                    .width(200.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 20.dp)
            ) {
                if (false) { // loading
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(stringResource(R.string.setupSetup))
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    SetupScreen()
}
