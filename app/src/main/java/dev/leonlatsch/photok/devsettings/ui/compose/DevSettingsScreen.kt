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

package dev.leonlatsch.photok.devsettings.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.devsettings.ui.DevSettingsUiEvent
import dev.leonlatsch.photok.devsettings.ui.DevSettingsUiState
import dev.leonlatsch.photok.devsettings.ui.DevSettingsViewModel
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.uicomponents.AppName

@Composable
fun DevSettingsScreen(
    viewModel: DevSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DevSettingsContent(
        uiState = uiState,
        handleUiEvent = viewModel::handleUiEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DevSettingsContent(
    uiState: DevSettingsUiState,
    handleUiEvent: (DevSettingsUiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .statusBarsPadding()
            ) {
                AppName()
                Text(
                    text = "Dev Settings",
                    fontFamily = FontFamily.Monospace
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = BuildConfig.VERSION_NAME,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(contentPadding),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fake has pro",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Overrides hasPro to true. Only has an effect in debug builds.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
                Switch(
                    checked = uiState.overrideHasPro,
                    onCheckedChange = { handleUiEvent(DevSettingsUiEvent.ToggleOverrideHasPro(it)) },
                )
            }
        }
    }
}

@Preview
@Composable
private fun DevSettingsScreenPreview() {
    AppTheme {
        DevSettingsContent(
            uiState = DevSettingsUiState(overrideHasPro = true),
            handleUiEvent = {},
        )
    }
}
