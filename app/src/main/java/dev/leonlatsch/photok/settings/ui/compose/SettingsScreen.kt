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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import dev.leonlatsch.photok.settings.domain.models.SettingsEnum
import dev.leonlatsch.photok.ui.theme.AppTheme

val LocalPreferencesValues: ProvidableCompositionLocal<Map<String, *>> = compositionLocalOf { emptyMap<String, String>() }

@Composable
fun SettingsScreen() {
    val viewModel = hiltViewModel<SettingsViewModel>()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CompositionLocalProvider(
        LocalPreferencesValues provides uiState.preferencesValues
    ) {
        SettingsContent(
            screenConfig = uiState.screenConfig,
            handleUiEvent = viewModel::handleUiEvent,
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    screenConfig: PreferenceScreenConfig,
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
                for (section in screenConfig.sections) {
                    val isLast = section == screenConfig.sections.last()

                    PreferenceSectionView(
                        section = section,
                    ) {
                        for (preference in section.preferences) {
                            when (preference) {
                                is Preference.Simple -> {
                                    PreferenceView(
                                        icon = painterResource(preference.icon),
                                        title = stringResource(preference.title),
                                        summary = stringResource(preference.summary),
                                    )
                                }
                                is Preference.Switch -> {
                                    PreferenceSwitchView(
                                        preference = preference,
                                        onSwitchChange = { key, value ->
                                            handleUiEvent(SettingsUiEvent.ToggleSwitch(key, value))
                                        },
                                    )
                                }
                                is Preference.Enum<*> -> {
                                    PreferenceEnumView(
                                        preference = preference,
                                        onItemSelected = { key, value ->
                                            handleUiEvent(
                                                SettingsUiEvent.SetEnumValue(
                                                    key,
                                                    value,
                                                )
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }

                    if (!isLast) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun PreferenceSectionView(
    section: PreferenceSection,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = stringResource(section.title),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : SettingsEnum> PreferenceEnumView(
    preference: Preference.Enum<T>,
    onItemSelected: (String, T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val preferencesValues = LocalPreferencesValues.current

    var showDialog by remember { mutableStateOf(false) }

    val rawValue = preferencesValues[preference.key] as? String ?: preference.default.value
    val value = preference.possibleValues.find { it.value == rawValue } ?: preference.default

    PreferenceView(
        icon = painterResource(preference.icon),
        title = stringResource(preference.title),
        summary = stringResource(value.label),
        onClick = { showDialog = true },
        modifier = modifier,
    )

    if (showDialog) {

        BasicAlertDialog(
            onDismissRequest = { showDialog = false },
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                ) {
                    Text(
                        text = stringResource(preference.title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    for (v in preference.possibleValues) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    showDialog = false
                                    onItemSelected(preference.key, v)
                                }
                        ) {
                            RadioButton(
                                selected = value == v,
                                onClick = {
                                    showDialog = false
                                    onItemSelected(preference.key, v)
                                },
                            )

                            Text(
                                text = stringResource(v.label),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    TextButton(
                        onClick = { showDialog = false },
                        modifier = Modifier.align(Alignment.End),
                    ) {
                        Text(stringResource(R.string.common_cancel))
                    }
                }
            }
        }
    }
}

@Composable
fun PreferenceSwitchView(
    preference: Preference.Switch,
    onSwitchChange: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val preferencesValues = LocalPreferencesValues.current
    
    val summary = stringResource(preference.summary)
    val value = preferencesValues[preference.key] as? Boolean ?: preference.default

    PreferenceView(
        icon = painterResource(preference.icon),
        title = stringResource(preference.title),
        summary = summary,
        trailing = {
            Switch(
                checked = value,
                onCheckedChange = {
                    onSwitchChange(preference.key, it)
                },
            )
        },
        onClick = {
            onSwitchChange(preference.key, !value)
        },
    )
}

@Composable
fun PreferenceView(
    icon: Painter,
    title: String,
    summary: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier
            .clickable(enabled = onClick != null) {
                onClick?.invoke()
            }
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
                screenConfig = PreferenceScreenConfig(PreferenceScreenConfigContent),
                handleUiEvent = {},
            )
        }
    }
}

