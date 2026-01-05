/*
 *   Copyright 2020–2026 Leon Latsch
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

package dev.leonlatsch.photok.settings.ui.hideapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.settings.ui.compose.LocalConfig
import dev.leonlatsch.photok.ui.theme.AppTheme
import kotlinx.coroutines.delay

const val LAUNCH_CODE_DEFAULT = "1337"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretLaunchCodeDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
) {
    val config = LocalConfig.current ?: error("SecretLaunchCodeDialog needs LocalConfig. Not provided")

    AppTheme {
        if (show) {
            var code by remember {
                val initial = config.securityDialLaunchCode.orEmpty()

                mutableStateOf(
                    TextFieldValue(
                        text = initial,
                        selection = TextRange(index = initial.length),
                    )
                )
            }
            val focusRequester = remember { FocusRequester() }

            LaunchedEffect(Unit) {
                delay(100)
                focusRequester.requestFocus()
            }

            AlertDialog(
                onDismissRequest = onDismissRequest,
                confirmButton = {
                    Button(
                        onClick = {
                            if (code.text.isEmpty()) {
                                config.securityDialLaunchCode = LAUNCH_CODE_DEFAULT
                            } else {
                                config.securityDialLaunchCode = code.text
                            }

                            onDismissRequest()
                        }
                    ) {
                        Text(stringResource(R.string.common_ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(R.string.common_cancel))
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.settings_security_launch_code_title)
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.settings_security_launch_code_message)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {

                            Text(
                                text = stringResource(R.string.settings_security_launch_code_prefix),
                            )
                            BasicTextField(
                                value = code,
                                onValueChange = {
                                    if (it.text.isEmpty() || it.text.length <= 10 && it.text.toIntOrNull() != null) {
                                        code = it
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                ),
                                maxLines = 1,
                                textStyle = LocalTextStyle.current.copy(
                                    textAlign = TextAlign.Center,
                                    color = LocalContentColor.current,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary),
                                decorationBox = { innerTextField ->
                                    val borderColor = MaterialTheme.colorScheme.primary

                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .drawWithContent {
                                                drawContent()
                                                drawLine(
                                                    color = borderColor,
                                                    start = Offset(0f, size.height),
                                                    end = Offset(this.size.width, size.height),
                                                    strokeWidth = Stroke.DefaultMiter,
                                                )
                                            }
                                    ) {
                                        if (code.text.isEmpty()) {
                                            Text(
                                                text = LAUNCH_CODE_DEFAULT,
                                                textAlign = TextAlign.Center,
                                                color = LocalContentColor.current.copy(alpha = 0.3f),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 16.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                },
                                modifier = Modifier
                                    .focusRequester(focusRequester)
                                    .widthIn(min = 80.dp)
                            )
                            Text(
                                text = stringResource(R.string.settings_security_launch_code_suffix)
                            )
                        }
                    }
                }
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    val context = LocalContext.current
    AppTheme() {
        CompositionLocalProvider(
            LocalConfig provides Config(context)
        ) {
            SecretLaunchCodeDialog(
                show = true,
                onDismissRequest = {},
            )
        }
    }
}