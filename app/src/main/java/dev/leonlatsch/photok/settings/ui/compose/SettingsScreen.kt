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

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.fragment.findNavController
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.backup.domain.BackupStrategy
import dev.leonlatsch.photok.backup.ui.BackupBottomSheetDialogFragment
import dev.leonlatsch.photok.databinding.BindingConverters
import dev.leonlatsch.photok.other.extensions.launchAndIgnoreTimer
import dev.leonlatsch.photok.other.extensions.show
import dev.leonlatsch.photok.other.openUrl
import dev.leonlatsch.photok.other.sendEmail
import dev.leonlatsch.photok.other.setAppDesign
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.settings.domain.Preference
import dev.leonlatsch.photok.settings.domain.PreferenceScreenConfig
import dev.leonlatsch.photok.settings.domain.PreferenceScreenConfigContent
import dev.leonlatsch.photok.settings.domain.PreferenceSection
import dev.leonlatsch.photok.settings.domain.models.SettingsEnum
import dev.leonlatsch.photok.settings.domain.models.SystemDesignEnum
import dev.leonlatsch.photok.settings.ui.SettingsFragment
import dev.leonlatsch.photok.settings.ui.changepassword.ChangePasswordDialog
import dev.leonlatsch.photok.settings.ui.checkpassword.CheckPasswordDialog
import dev.leonlatsch.photok.settings.ui.hideapp.SecretLaunchCodeDialog
import dev.leonlatsch.photok.settings.ui.hideapp.ToggleAppVisibilityDialog
import dev.leonlatsch.photok.ui.LocalFragment
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.uicomponnets.Dialogs

val LocalPreferencesValues: ProvidableCompositionLocal<Map<String, *>> = compositionLocalOf { emptyMap<String, String>() }

fun createBackupFilename(): String {
    return "photok_backup_${BindingConverters.millisToFormattedDateConverter(System.currentTimeMillis())}.zip"
}

@Composable
fun SettingsCallbacks(viewModel: SettingsViewModel) {
    val fragment = LocalFragment.current
    val context = LocalContext.current
    val activity = LocalActivity.current

    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        fragment ?: return@rememberLauncherForActivityResult
        BackupBottomSheetDialogFragment(uri, BackupStrategy.Name.Default).show(fragment.parentFragmentManager)
    }

    var showSecretLaunchCodeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        fragment ?: return@LaunchedEffect

        viewModel.registerPreferenceCallback(Config.SYSTEM_DESIGN) {
            it as SystemDesignEnum
            setAppDesign(it)
            true
        }

        viewModel.registerPreferenceCallback(SettingsFragment.KEY_ACTION_CHANGE_PASSWORD) {
            ChangePasswordDialog().show(fragment.childFragmentManager)
            false
        }

        viewModel.registerPreferenceCallback(Config.SECURITY_BIOMETRIC_AUTHENTICATION_ENABLED) {
            viewModel.onBiometricUnlockChanged(it, fragment)
        }

        viewModel.registerPreferenceCallback(Config.SECURITY_DIAL_LAUNCH_CODE) {
            showSecretLaunchCodeDialog = true
            false
        }

        viewModel.registerPreferenceCallback(SettingsFragment.KEY_ACTION_HIDE_APP) {
            ToggleAppVisibilityDialog().show(fragment.childFragmentManager)
            false
        }

        viewModel.registerPreferenceCallback(SettingsFragment.KEY_ACTION_RESET) {
            CheckPasswordDialog {
                Dialogs.showConfirmDialog(
                    context,
                    context.getString(R.string.settings_advanced_reset_confirmation)
                ) { _, _ ->
                    viewModel.resetComponents()
                }
            }.show(fragment.childFragmentManager)
            false
        }

        viewModel.registerPreferenceCallback(SettingsFragment.KEY_ACTION_BACKUP) {
            backupLauncher.launchAndIgnoreTimer(
                createBackupFilename(),
                activity = activity,
            )
            false
        }

        viewModel.registerPreferenceCallback(SettingsFragment.KEY_ACTION_FEEDBACK) {
            val email = context.getString(R.string.settings_other_feedback_mail_emailaddress)
            val subject =
                "${context.getString(R.string.settings_other_feedback_mail_subject)} (App ${BuildConfig.VERSION_NAME} / Android ${Build.VERSION.RELEASE})"
            val text = context.getString(R.string.settings_other_feedback_mail_body)

            context.sendEmail(
                email = email,
                subject = subject,
                text = text,
                chooserTitle = context.getString(R.string.settings_other_feedback_title)
            )
            false
        }

        viewModel.registerPreferenceCallback(SettingsFragment.KEY_ACTION_DONATE) {
            fragment.openUrl(context.getString(R.string.settings_other_donate_url))
            false
        }

        viewModel.registerPreferenceCallback(SettingsFragment.KEY_ACTION_SOURCECODE) {
            fragment.openUrl(context.getString(R.string.settings_other_sourcecode_url))
            false
        }

        viewModel.registerPreferenceCallback(SettingsFragment.KEY_ACTION_CREDITS) {
            fragment.findNavController().navigate(R.id.action_settingsFragment_to_creditsFragment)
            false
        }

        viewModel.registerPreferenceCallback(SettingsFragment.KEY_ACTION_ABOUT) {
            fragment.findNavController().navigate(R.id.action_settingsFragment_to_aboutFragment)
            false
        }
    }

    SecretLaunchCodeDialog(
        show = showSecretLaunchCodeDialog,
        onDismissRequest = { showSecretLaunchCodeDialog = false },
    )
}

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

    SettingsCallbacks(viewModel)

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    screenConfig: PreferenceScreenConfig,
    handleUiEvent: (SettingsUiEvent) -> Unit,
) {
    val fragment = LocalFragment.current

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
                                        onClick = {
                                            fragment ?: return@PreferenceView
                                            handleUiEvent(SettingsUiEvent.OnPreferenceClick(preference, null))
                                        }
                                    )
                                }
                                is Preference.Switch -> {
                                    PreferenceSwitchView(
                                        preference = preference,
                                        onSwitchChange = { value ->
                                            fragment ?: return@PreferenceSwitchView
                                            handleUiEvent(SettingsUiEvent.OnPreferenceClick(preference, value))
                                        },
                                    )
                                }
                                is Preference.Enum<*> -> {
                                    PreferenceEnumView(
                                        preference = preference,
                                        onItemSelected = { value ->
                                            fragment ?: return@PreferenceEnumView
                                            handleUiEvent(SettingsUiEvent.OnPreferenceClick(preference, value))
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
        )

        if (section.summary != null) {
            Text(
                text = stringResource(section.summary),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .padding(
                        horizontal = 60.dp
                    )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : SettingsEnum> PreferenceEnumView(
    preference: Preference.Enum<T>,
    onItemSelected: (T) -> Unit,
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
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false },
                ) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
            title = {
                Text(
                    text = stringResource(preference.title),
                )
            },
            text = {
                Column {
                    for (v in preference.possibleValues) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    showDialog = false
                                    onItemSelected(v)
                                }
                        ) {
                            RadioButton(
                                selected = value == v,
                                onClick = {
                                    showDialog = false
                                    onItemSelected(v)
                                },
                            )

                            Text(
                                text = stringResource(v.label),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
        )
    }
}

@Composable
fun PreferenceSwitchView(
    preference: Preference.Switch,
    onSwitchChange: (Boolean) -> Unit,
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
                    onSwitchChange(it)
                },
            )
        },
        onClick = {
            onSwitchChange(!value)
        },
        modifier = modifier
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

