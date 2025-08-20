/*
 *   Copyright 2020-2024 Leon Latsch
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

package dev.leonlatsch.photok.security.migration.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.components.AppName
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun EncryptionMigrationScreenError(
    uiState: LegacyEncryptionMigrationUiState.Error
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.errorContainer,
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {

                AppName()

                Text(
                    text = "Something went wrong",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        modifier = Modifier
                            .size(IconSize),
                        painter = painterResource(R.drawable.ic_warning),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )

                    Text(
                        text = uiState.error.message ?: stringResource(R.string.common_error),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}





@PreviewLightDark
@Composable
private fun PreviewError() {
    AppTheme {
        EncryptionMigrationScreenError(
            uiState = LegacyEncryptionMigrationUiState.Error(Throwable("Test error"))
        )
    }
}
