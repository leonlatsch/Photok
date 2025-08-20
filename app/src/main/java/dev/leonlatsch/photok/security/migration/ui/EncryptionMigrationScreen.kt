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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
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

private val IconSize = 72.dp

@Composable
fun EncryptionMigrationScreenMigrating(
    uiState: LegacyEncryptionMigrationUiState.Migrating,
) {
    Scaffold { contentPadding ->
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
                    text = "Migrating your gallery",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LoadingIndicator(
                        progressPercentage = uiState.progressPercentage
                    )

                    Text(
                        text = "${uiState.processedFiles} of ${uiState.totalFiles} files processed",
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }

            Text(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                text = "Feel free to leave this screen while the migration is running",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

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

@Composable
fun EncryptionMigrationScreenSuccess(
    uiState: LegacyEncryptionMigrationUiState.Success
) {
    Scaffold { contentPadding ->
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
                    text = "Migrating your gallery",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LoadingIndicator(
                        progressPercentage = 1f
                    )

                    Text(
                        text = "All files migrated",
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }

            Text(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                text = "Feel free to leave this screen while the migration is running",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun LoadingIndicator(progressPercentage: Float, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Image(
            modifier = Modifier
                .align(Alignment.Center)
                .size(IconSize),
            painter = painterResource(R.drawable.app_icon),
            contentDescription = null
        )

        CircularProgressIndicator(
            progress = { progressPercentage },
            modifier = Modifier
                .align(Alignment.Center)
                .size(IconSize + 40.dp),
            strokeWidth = 10.dp,
        )
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    AppTheme {
        EncryptionMigrationScreenMigrating(
            uiState = LegacyEncryptionMigrationUiState.Migrating(
                totalFiles = 100,
                processedFiles = 40,
            ),
        )
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

@PreviewLightDark
@Composable
private fun PreviewSuccess() {
    AppTheme {
        EncryptionMigrationScreenSuccess(
            uiState = LegacyEncryptionMigrationUiState.Success
        )
    }
}
