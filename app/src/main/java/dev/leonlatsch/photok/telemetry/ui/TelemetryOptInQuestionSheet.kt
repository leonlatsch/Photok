/*
 *   Copyright 2020-2026 Leon Latsch
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

package dev.leonlatsch.photok.telemetry.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices.NEXUS_5
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.other.openUrl
import dev.leonlatsch.photok.settings.ui.compose.LocalConfig
import dev.leonlatsch.photok.telemetry.domain.TelemetryEnabledByDefault
import dev.leonlatsch.photok.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TelemetryOptInQuestionSheet() {
    val config = LocalConfig.current
    val viewModel: TelemetryViewModel = hiltViewModel()

    var visible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        config ?: return@LaunchedEffect

        if (!TelemetryEnabledByDefault && !config.telemetryAskedForOptIn && !config.justFinishedSetup) {
            delay(300)
            visible = true
            config.telemetryAskedForOptIn = true
        }
    }

    if (visible) {
        SheetContent(
            updateEnabled = {
                viewModel.updateTelemetryEnabled(it)
            },
            onDismissRequest = { visible = false },
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetContent(
    updateEnabled: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val state = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = onDismissRequest,
        dragHandle = null,
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
            text = stringResource(R.string.telemetry_sheet_title),
            style = MaterialTheme.typography.titleLarge,
        )

            Text(
                text = stringResource(R.string.telemetry_sheet_paragraph_1)
            )

            Text(
                text = stringResource(R.string.telemetry_sheet_paragraph_2)
            )

            Text(
                text = stringResource(R.string.telemetry_sheet_paragraph_3)
            )

            val context = LocalContext.current
            val ppUrl = stringResource(R.string.about_privacy_policy_url)

            TextButton(
                onClick = {
                    context.openUrl(ppUrl)
                }
            ) {
                Text(stringResource(R.string.telemetry_learn_more))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = {
                        updateEnabled(false)
                        scope.launch {
                            state.hide()
                        }.invokeOnCompletion { onDismissRequest() }
                    },
                ) {
                    Text(stringResource(R.string.telemetry_choice_no))
                }

                Button(
                    onClick = {
                        updateEnabled(true)
                        scope.launch {
                            state.hide()
                        }.invokeOnCompletion { onDismissRequest() }
                    }
                ) {
                    Text(stringResource(R.string.telemetry_choice_yes))
                }
            }
        }
    }
}

@Preview(device = NEXUS_5)
@Composable
private fun Preview() {
    AppTheme {
        SheetContent(
            updateEnabled = {},
            onDismissRequest = {},
        )
    }
}