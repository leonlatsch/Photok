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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.devsettings.domain.PreferenceEntry
import dev.leonlatsch.photok.devsettings.domain.PreferenceFile
import dev.leonlatsch.photok.devsettings.ui.PreferencesEditorUiEvent
import dev.leonlatsch.photok.devsettings.ui.PreferencesEditorUiState
import dev.leonlatsch.photok.devsettings.ui.PreferencesEditorViewModel
import dev.leonlatsch.photok.navigation.LocalNavigator
import dev.leonlatsch.photok.ui.theme.AppTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private const val MIN_DATE_MILLIS = 946_684_800_000L
private const val MAX_DATE_MILLIS = 4_102_444_800_000L

private val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)

private data class EditingEntry(val file: String, val entry: PreferenceEntry)

@Composable
fun PreferencesEditorScreen(
    viewModel: PreferencesEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current

    PreferencesEditorContent(
        uiState = uiState,
        handleUiEvent = viewModel::handleUiEvent,
        onClose = navigator::goBack,
    )
}

@Composable
private fun PreferencesEditorContent(
    uiState: PreferencesEditorUiState,
    handleUiEvent: (PreferencesEditorUiEvent) -> Unit,
    onClose: () -> Unit,
) {
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.CenterStart),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = stringResource(R.string.process_close),
                    )
                }
                Text(
                    text = "Shared Preferences",
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    ) { contentPadding ->
        when (uiState) {
            is PreferencesEditorUiState.Loading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                ) {
                    CircularProgressIndicator()
                }
            }

            is PreferencesEditorUiState.Content -> {
                PreferencesList(
                    files = uiState.files,
                    handleUiEvent = handleUiEvent,
                    modifier = Modifier.padding(contentPadding),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PreferencesList(
    files: List<PreferenceFile>,
    handleUiEvent: (PreferencesEditorUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editing by remember { mutableStateOf<EditingEntry?>(null) }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        files.forEach { file ->
            stickyHeader(key = file.name) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            if (file.entries.isEmpty()) {
                item(key = "${file.name}/empty") {
                    Text(
                        text = "No entries",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            items(file.entries, key = { "${file.name}/${it.key}" }) { entry ->
                PreferenceRow(
                    entry = entry,
                    onClick = { editing = EditingEntry(file.name, entry) },
                    onToggle = { handleUiEvent(PreferencesEditorUiEvent.SaveBoolean(file.name, entry.key, it)) },
                )
                HorizontalDivider()
            }
        }
    }

    editing?.let { current ->
        val file = current.file
        val dismiss = { editing = null }
        val remove = {
            handleUiEvent(PreferencesEditorUiEvent.Remove(file, current.entry.key))
            editing = null
        }

        when (val entry = current.entry) {
            is PreferenceEntry.StringEntry -> TextValueDialog(
                key = entry.key,
                initialValue = entry.value,
                keyboardType = KeyboardType.Text,
                isValid = { true },
                onSave = {
                    handleUiEvent(PreferencesEditorUiEvent.SaveString(file, entry.key, it))
                    editing = null
                },
                onDelete = remove,
                onDismiss = dismiss,
            )

            is PreferenceEntry.IntEntry -> TextValueDialog(
                key = entry.key,
                initialValue = entry.value.toString(),
                keyboardType = KeyboardType.Number,
                isValid = { it.toIntOrNull() != null },
                onSave = {
                    handleUiEvent(PreferencesEditorUiEvent.SaveInt(file, entry.key, it.toInt()))
                    editing = null
                },
                onDelete = remove,
                onDismiss = dismiss,
            )

            is PreferenceEntry.FloatEntry -> TextValueDialog(
                key = entry.key,
                initialValue = entry.value.toString(),
                keyboardType = KeyboardType.Decimal,
                isValid = { it.toFloatOrNull() != null },
                onSave = {
                    handleUiEvent(PreferencesEditorUiEvent.SaveFloat(file, entry.key, it.toFloat()))
                    editing = null
                },
                onDelete = remove,
                onDismiss = dismiss,
            )

            is PreferenceEntry.LongEntry -> LongValueDialog(
                key = entry.key,
                initialValue = entry.value,
                onSave = {
                    handleUiEvent(PreferencesEditorUiEvent.SaveLong(file, entry.key, it))
                    editing = null
                },
                onDelete = remove,
                onDismiss = dismiss,
            )

            is PreferenceEntry.BooleanEntry,
            is PreferenceEntry.StringSetEntry -> ReadOnlyValueDialog(
                key = entry.key,
                value = entry.displayValue(),
                onDelete = remove,
                onDismiss = dismiss,
            )
        }
    }
}

@Composable
private fun PreferenceRow(
    entry: PreferenceEntry,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.key,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                text = entry.typeName(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
            if (entry !is PreferenceEntry.BooleanEntry) {
                Text(
                    text = entry.displayValue(),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                )
            }
            if (entry is PreferenceEntry.LongEntry && entry.value.looksLikeDate()) {
                Text(
                    text = formatDate(entry.value),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }

        if (entry is PreferenceEntry.BooleanEntry) {
            Switch(
                checked = entry.value,
                onCheckedChange = onToggle,
            )
        }
    }
}

@Composable
private fun TextValueDialog(
    key: String,
    initialValue: String,
    keyboardType: KeyboardType,
    isValid: (String) -> Boolean,
    onSave: (String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = key, fontFamily = FontFamily.Monospace) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                isError = !isValid(text),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(text) },
                enabled = isValid(text),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LongValueDialog(
    key: String,
    initialValue: Long,
    onSave: (Long) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(initialValue.toString()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var pickedDate by remember { mutableStateOf<LocalDate?>(null) }
    val parsed = text.toLongOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = key, fontFamily = FontFamily.Monospace) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    isError = parsed == null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = {
                        if (parsed != null && parsed.looksLikeDate()) {
                            Text(formatDate(parsed))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(onClick = { showDatePicker = true }) {
                    Text("Pick date")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { parsed?.let(onSave) },
                enabled = parsed != null,
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
    )

    val initialDateTime = (parsed?.takeIf { it.looksLikeDate() } ?: System.currentTimeMillis()).toLocalDateTime()

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateTime.toLocalDate()
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickedDate = datePickerState.selectedDateMillis?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        }
                        showDatePicker = false
                    },
                ) {
                    Text("Next")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    pickedDate?.let { date ->
        val timePickerState = rememberTimePickerState(
            initialHour = initialDateTime.hour,
            initialMinute = initialDateTime.minute,
        )
        AlertDialog(
            onDismissRequest = { pickedDate = null },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        text = LocalDateTime.of(date, LocalTime.of(timePickerState.hour, timePickerState.minute))
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                            .toString()
                        pickedDate = null
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { pickedDate = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun ReadOnlyValueDialog(
    key: String,
    value: String,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = key, fontFamily = FontFamily.Monospace) },
        text = { Text(text = value, fontFamily = FontFamily.Monospace) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        dismissButton = {
            TextButton(onClick = onDelete) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
    )
}

private fun PreferenceEntry.typeName(): String = when (this) {
    is PreferenceEntry.StringEntry -> "String"
    is PreferenceEntry.IntEntry -> "Int"
    is PreferenceEntry.LongEntry -> "Long"
    is PreferenceEntry.FloatEntry -> "Float"
    is PreferenceEntry.BooleanEntry -> "Boolean"
    is PreferenceEntry.StringSetEntry -> "Set<String>"
}

private fun PreferenceEntry.displayValue(): String = when (this) {
    is PreferenceEntry.StringEntry -> value
    is PreferenceEntry.IntEntry -> value.toString()
    is PreferenceEntry.LongEntry -> value.toString()
    is PreferenceEntry.FloatEntry -> value.toString()
    is PreferenceEntry.BooleanEntry -> value.toString()
    is PreferenceEntry.StringSetEntry -> value.joinToString(prefix = "[", postfix = "]")
}

private fun Long.looksLikeDate(): Boolean = this in MIN_DATE_MILLIS..MAX_DATE_MILLIS

private fun Long.toLocalDateTime(): LocalDateTime =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()

private fun formatDate(millis: Long): String = millis.toLocalDateTime().format(dateTimeFormatter)

@Preview
@Composable
private fun PreferencesEditorScreenPreview() {
    AppTheme {
        PreferencesEditorContent(
            uiState = PreferencesEditorUiState.Content(
                files = listOf(
                    PreferenceFile(
                        name = "dev.leonlatsch.photok_preferences",
                        entries = listOf(
                            PreferenceEntry.BooleanEntry("ui^darkMode", true),
                            PreferenceEntry.IntEntry("security^lockTimeout", 300000),
                            PreferenceEntry.LongEntry("system^installDate", 1_760_000_000_000L),
                            PreferenceEntry.StringEntry("gallery^sort", "importedAt"),
                        ),
                    ),
                ),
            ),
            handleUiEvent = {},
            onClose = {},
        )
    }
}
