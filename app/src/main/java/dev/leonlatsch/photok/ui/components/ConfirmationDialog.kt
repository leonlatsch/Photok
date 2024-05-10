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

package dev.leonlatsch.photok.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun ConfirmationDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    text: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (show) {
        AlertDialog(
            modifier = modifier,
            onDismissRequest = onDismissRequest,
            dismissButton = {
                TextButton(onClick = { onDismissRequest() }) {
                    Text(text = stringResource(R.string.common_no))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                        onDismissRequest()
                    }
                ) {
                    Text(text = stringResource(R.string.common_yes))
                }
            },
            text = {
                Text(text = text)
            }
        )
    }
}

@Preview
@Composable
private fun ConfirmationDialogPreview() {
    AppTheme {
        ConfirmationDialog(
            show = true,
            onDismissRequest = { /*TODO*/ },
            text = "This is a simple example",
            onConfirm = { /*TODO*/ }
        )
    }
}
