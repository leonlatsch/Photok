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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.components.AppName
import dev.leonlatsch.photok.ui.theme.AppTheme
import okio.IOException

@Composable
fun EncryptionMigrationScreenError(
    uiState: LegacyEncryptionMigrationUiState.Error,
    handleUiEvent: (LegacyEncryptionMigrationUiEvent) -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.errorContainer,
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {

                AppName()

                Text(
                    text = stringResource(R.string.migration_error_title),
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        modifier = Modifier
                            .padding(vertical = 24.dp)
                            .size(IconSize),
                        painter = painterResource(R.drawable.ic_smile_sad),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )

                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                        onClick = {
                            handleUiEvent(
                                LegacyEncryptionMigrationUiEvent.StartMigration(context)
                            )
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_refresh),
                                contentDescription = null,
                            )
                            Text(stringResource(R.string.common_try_again))
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                        text = uiState.error.localizedMessage
                            ?: stringResource(R.string.common_error),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            TextButton(
                modifier = Modifier
                    .padding(20.dp)
                    .align(Alignment.BottomCenter),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
                onClick = {
                    handleUiEvent(
                        LegacyEncryptionMigrationUiEvent.SendErrorReport(
                            context,
                            uiState.error
                        )
                    )
                },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        painterResource(R.drawable.ic_email),
                        contentDescription = null,
                    )
                    Text(stringResource(R.string.migration_error_button))
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
            uiState = LegacyEncryptionMigrationUiState.Error(
                IOException("Some error message from exception skdjgsjdgijsdgjskdjgiosdjigvjskjvskdjfgsdlkgvmskdgkskdgklskgljasöflkvmlkasgvsajgkljsafkgjlsöakvlkösjakvmasdklgjfsaklmgdlkj Some error message from exception skdjgsjdgijsdgjskdjgiosdjigvjskjvskdjfgsdlkgvmskdgkskdgklskgljasöflkvmlkasgvsajgkljsafkgjlsöakvlkösjakvmasdklgjfsaklmgdlkjSome error message from exception skdjgsjdgijsdgjskdjgiosdjigvjskjvskdjfgsdlkgvmskdgkskdgklskgljasöflkvmlkasgvsajgkljsafkgjlsöakvlkösjakvmasdklgjfsaklmgdlkjSome error message from exception skdjgsjdgijsdgjskdjgiosdjigvjskjvskdjfgsdlkgvmskdgkskdgklskgljasöflkvmlkasgvsajgkljsafkgjlsöakvlkösjakvmasdklgjfsaklmgdlkjSome error message from exception skdjgsjdgijsdgjskdjgiosdjigvjskjvskdjfgsdlkgvmskdgkskdgklskgljasöflkvmlkasgvsajgkljsafkgjlsöakvlkösjakvmasdklgjfsaklmgdlkj")
            ),
            handleUiEvent = {},
        )
    }
}
