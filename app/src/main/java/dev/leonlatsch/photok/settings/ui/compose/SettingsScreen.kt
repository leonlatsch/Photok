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

package dev.leonlatsch.photok.settings.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.settings.domain.models.SettingsEntry
import dev.leonlatsch.photok.ui.theme.AppTheme


@Composable
fun SettingsScreen() {
    val viewModel = hiltViewModel<SettingsViewModel>()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsContent(
        uiState = uiState,
        handleUiEvent = viewModel::handleUiEvent,
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    handleUiEvent: (SettingsUiEvent) -> Unit,
) {
    AppTheme {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        Scaffold(
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.settings_title)
                        )
                    },
                    scrollBehavior = scrollBehavior,
                )
            }
        ) { contentPadding ->
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding)
            ) {
                SettingsSection("App") {
                    SettingsRow(
                        icon = painterResource(R.drawable.ic_brush),
                        title = stringResource(R.string.settings_app_design_title),
                        summary = "System Design",
                    )
                }

                HorizontalDivider()

                SettingsSection("Gallery") {
                    SettingsSwitchRow(
                        values = uiState.values,
                        onSwitchChange = { key, value ->
                            handleUiEvent(SettingsUiEvent.ToggleSwitch(key, value))
                        },
                        entry = Config.GalleryAutoFullscreen,
                    )
                    SettingsRow(
                        icon = painterResource(R.drawable.ic_gallery_thumbnail),
                        title = stringResource(R.string.settings_gallery_start_page_title),
                        summary = "All Files",
                    )
                }

                HorizontalDivider()

                SettingsSection("Security") {

                    SettingsSwitchRow(
                        values = uiState.values,
                        onSwitchChange = { key, value ->
                            handleUiEvent(SettingsUiEvent.ToggleSwitch(key, value))
                        },
                        entry = Config.SecurityAllowScreenshots,
                    )
                    SettingsRow(
                        icon = painterResource(R.drawable.ic_key),
                        title = stringResource(R.string.settings_security_change_password_title),
                        summary = stringResource(R.string.settings_security_change_password_summary),
                    )
                    SettingsRow(
                        icon = painterResource(R.drawable.ic_fingerprint),
                        title = stringResource(R.string.settings_security_biometric_title),
                        summary = stringResource(R.string.settings_security_biometric_summary),
                        trailing = {
                            Switch(
                                checked = false,
                                onCheckedChange = {},
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    headline: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = headline,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier
                .padding(
                    horizontal = 60.dp
                )
                .padding(
                    bottom = 10.dp
                )
        )

         content()
    }
}

@Composable
fun SettingsSwitchRow(
    entry: SettingsEntry<Boolean>,
    values: Map<String, *>,
    onSwitchChange: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val summary = if (entry.summary != null) {
        stringResource(entry.summary)
    } else {
        null
    }

    val value = values[entry.key] as? Boolean ?: entry.default

    SettingsRow(
        icon = painterResource(entry.icon),
        title = stringResource(entry.title),
        summary = summary ?: values[entry.key].toString(),
        trailing = {
            Switch(
                checked = value,
                onCheckedChange = {
                    onSwitchChange(entry.key, it)
                },
            )
        },
        modifier = modifier
            .clickable {
                onSwitchChange(entry.key, !value)
            }
    )
}

@Composable
fun SettingsRow(
    icon: Painter,
    title: String,
    summary: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp,
                vertical = 12.dp,
            )
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = summary,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        if (trailing != null) {
            trailing()
        }
    }

}

@PreviewLightDark
@Composable
private fun Preview() {
    val context = LocalContext.current
    CompositionLocalProvider(LocalConfig provides Config(context)) {
        AppTheme {
            SettingsContent(
                uiState = SettingsUiState(emptyMap<String, String>()),
                handleUiEvent = {},
            )
        }
    }
}

