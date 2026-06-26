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

package dev.leonlatsch.photok.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    imeAction: ImeAction = ImeAction.Done,
    onDone: (() -> Unit)? = null,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction,
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDone?.invoke() },
        ),
        isError = error != null,
        supportingText = if (error != null) {
            { Text(error) }
        } else null,
        maxLines = 1,
        shape = RoundedCornerShape(24.dp),
        suffix = {
            val icon = if (passwordVisible) R.drawable.ic_eye_closed else R.drawable.ic_eye
            Crossfade(targetState = icon, label = "PasswordVisibility") { iconRes ->
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(24.dp)
                        .clickable(role = Role.Button) { passwordVisible = !passwordVisible }
                )
            }
        },
        modifier = modifier.fillMaxWidth(),
    )
}

@PreviewLightDark
@Composable
private fun Preview() {
    AppTheme {
        Surface {
            PasswordField(
                value = "secret",
                onValueChange = {},
                label = "Password",
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewError() {
    AppTheme {
        Surface {
            PasswordField(
                value = "secret",
                onValueChange = {},
                label = "Password",
                error = "⚠ Passwords do not match",
            )
        }
    }
}
