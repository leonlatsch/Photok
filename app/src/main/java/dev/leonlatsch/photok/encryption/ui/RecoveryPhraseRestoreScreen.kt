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

package dev.leonlatsch.photok.encryption.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class AnimatedWord(
    val id: Long,
    val text: String,
    val visible: Boolean = false,  // drives enter animation: starts false, toggled true after first frame
    val removing: Boolean = false, // drives exit animation: set true before delayed removal
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecoveryPhraseRestoreScreen(
    onRestored: () -> Unit,
) {
    val viewModel: RecoveryPhraseRestoreViewModel = hiltViewModel()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.unlocked) {
        if (uiState.unlocked) {
            delay(3000)
            onRestored()
        }
    }

    AppTheme {
        RecoveryPhraseRestoreContent(
            uiState = uiState,
            handleUiEvent = viewModel::handleUiEvent,
        )
    }
}

@Composable
private fun RecoveryPhraseRestoreContent(
    uiState: RecoveryPhraseRestoreUiState,
    handleUiEvent: (RecoveryPhraseRestoreUiEvent) -> Unit,
) {
    Scaffold(
        bottomBar = {
            Crossfade(uiState.unlocked) {
                if (!it) {
                    Button(
                        onClick = {
                            handleUiEvent(RecoveryPhraseRestoreUiEvent.Restore(uiState.words))
                        },
                        enabled = uiState.validInput,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(horizontal = 20.dp)
                            .fillMaxWidth()
                    ) {
                        Text(text = stringResource(R.string.recovery_phrase_restore_button))
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { contentPadding ->
        AnimatedVisibility(
            visible = !uiState.unlocked
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.recovery_phrase_restore_title),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.recovery_phrase_restore_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(24.dp))

                RecoveryPhraseInputField(
                    onWordsChanged = {
                        handleUiEvent(RecoveryPhraseRestoreUiEvent.UpdateWords(it))
                    }
                )

                AnimatedVisibility(uiState.error != null) {
                    Text(
                        text = "Could not restore vault from recovery phrase.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecoveryPhraseInputField(
    onWordsChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var wordItems by remember { mutableStateOf(listOf<AnimatedWord>()) }
    var currentInput by remember { mutableStateOf("") }

    fun confirmWord(word: String) {
        val newItem = AnimatedWord(id = System.nanoTime(), text = word)
        val newList = wordItems + newItem
        wordItems = newList
        onWordsChanged(newList.filter { !it.removing }.map { it.text })
        scope.launch {
            delay(16) // allow the item to enter composition before animating in
            wordItems = wordItems.map { if (it.id == newItem.id) it.copy(visible = true) else it }
        }
    }

    fun removeLastWord() {
        val last = wordItems.lastOrNull { !it.removing } ?: return
        currentInput = last.text
        val newList = wordItems.map { if (it.id == last.id) it.copy(removing = true) else it }
        wordItems = newList
        onWordsChanged(newList.filter { !it.removing }.map { it.text })
        scope.launch {
            delay(300) // match exit animation duration
            wordItems = wordItems.filter { it.id != last.id }
        }
    }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        wordItems.forEachIndexed { index, word ->
            key(word.id) {
                AnimatedVisibility(
                    visible = word.visible && !word.removing,
                    enter = slideInHorizontally { it } + fadeIn(animationSpec = tween(120)),
                    exit = slideOutHorizontally { it } + fadeOut(animationSpec = tween(150)),
                ) {
                    WordChip(
                        number = index + 1,
                        word = word.text,
                    )
                }
            }
        }

        WordInputChip(
            number = wordItems.count { !it.removing } + 1,
            value = currentInput,
            onValueChange = { input ->
                // Handle space (or paste with multiple words) — split and confirm all complete words
                val parts = input.split(" ")
                parts.dropLast(1).filter { it.isNotBlank() }.forEach { confirmWord(it) }
                currentInput = parts.last()
            },
            onBackspaceOnEmpty = ::removeLastWord,
        )
    }
}

// A zero-width space is prepended to every TextFieldValue so that when the
// soft keyboard deletes it, we detect "backspace on empty" reliably.
private const val SENTINEL = "\u200B"

@Composable
private fun WordInputChip(
    number: Int,
    value: String,
    onValueChange: (String) -> Unit,
    onBackspaceOnEmpty: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var fieldValue by remember {
        mutableStateOf(TextFieldValue(SENTINEL + value, TextRange(SENTINEL.length + value.length)))
    }

    // Sync when the parent resets or restores the value (e.g. after confirming a word)
    LaunchedEffect(value) {
        val expected = SENTINEL + value
        if (fieldValue.text != expected) {
            fieldValue = TextFieldValue(expected, TextRange(expected.length))
        }
    }

    BasicTextField(
        value = fieldValue,
        onValueChange = { new ->
            if (!new.text.startsWith(SENTINEL)) {
                // The sentinel was consumed by backspace — field was effectively empty
                fieldValue = TextFieldValue(SENTINEL, TextRange(SENTINEL.length))
                onBackspaceOnEmpty()
            } else {
                fieldValue = new
                onValueChange(new.text.removePrefix(SENTINEL))
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
        ),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        ),
        modifier = modifier.widthIn(min = 80.dp),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp),
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$number",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Box(contentAlignment = Alignment.Center) {
                        if (value.isEmpty()) {
                            Text(
                                text = "...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center,
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            )
                        }
                        innerTextField()
                    }
                }
            }
        }
    )
}

@PreviewLightDark
@Composable
private fun Preview() {
    AppTheme {
        RecoveryPhraseRestoreContent(
            uiState = RecoveryPhraseRestoreUiState(
                words = listOf("this", "is"),
                validInput = false,
                loading = false,
            ),
            handleUiEvent = {},
        )
    }
}
@PreviewLightDark
@Composable
private fun PreviewLoading() {
    AppTheme {
        RecoveryPhraseRestoreContent(
            uiState = RecoveryPhraseRestoreUiState(
                words = listOf("this", "is"),
                validInput = true,
                loading = true,
            ),
            handleUiEvent = {},
        )
    }
}
