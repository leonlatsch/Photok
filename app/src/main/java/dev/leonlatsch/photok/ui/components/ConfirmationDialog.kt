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
