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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.other.openUrl
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.telemetry.domain.TelemetryService
import dev.leonlatsch.photok.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class TelemetryExplanationViewModel @Inject constructor(
    private val config: Config,
    private val telemetryService: TelemetryService,
) : ViewModel() {
    val enabled = MutableStateFlow(config.telemetryEnabled) // TODO: Make this reactive

    fun updateTelemetryEnabled(enabled: Boolean) {
        config.telemetryEnabled = enabled
        this.enabled.value = enabled
        telemetryService.setup()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelemetryExplanationSheet(visible: Boolean, onDismissRequest: () -> Unit) {
    if (visible) {
        val viewModel: TelemetryExplanationViewModel = hiltViewModel()
        val enabledState by viewModel.enabled.collectAsStateWithLifecycle()

        val state = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

        AppTheme {
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
                        text = "Usage Data Collection", // TODO
                        style = MaterialTheme.typography.titleLarge,
                    )

                    Text(
                        text = "Photok uses a privacy friendly analytics service called TelemetryDeck.", // TODO
                    )

                    Text(
                        text = "The data processed by TelemetryDeck is completely anonymized and does not allow any conclusions to be drawn about personal information." // TODO
                    )

                    val context = LocalContext.current
                    val ppUrl = stringResource(R.string.about_privacy_policy_url)

                    TextButton(
                        onClick = {
                            context.openUrl(ppUrl)
                        }
                    ) {
                        Text("Learn more") // TODO
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Allow collection of usage data", // TODO
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = enabledState,
                            onCheckedChange = {
                                viewModel.updateTelemetryEnabled(it)
                            }
                        )
                    }

                }
            }
        }
    }
}