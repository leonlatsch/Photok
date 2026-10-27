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

package dev.leonlatsch.photok.onboarding.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.PasswordUtils
import dev.leonlatsch.photok.encryption.domain.models.KekSpec
import dev.leonlatsch.photok.other.openUrl
import dev.leonlatsch.photok.ui.systembars.StatusBarBackground
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.theme.Dimens
import java.text.NumberFormat

private const val ZXCVBN4J_URL = "https://github.com/nulab/zxcvbn4j"
private const val ZXCVBN_URL = "https://github.com/dropbox/zxcvbn"
private const val ENCRYPTION_URL = "https://github.com/leonlatsch/Photok/blob/main/ENCRYPTION.md"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrackTimeExplanationSheet(onDismissRequest: () -> Unit) {
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onDismissRequest = onDismissRequest,
        contentWindowInsets = { BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Top) },
    ) {
        CrackTimeExplanationContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CrackTimeExplanationContent() {
    val numberFormat = NumberFormat.getIntegerInstance(LocalConfiguration.current.locales[0])
    val guessesPerSecond = numberFormat.format(PasswordUtils.crackTimeGuessesPerSecond)

    StatusBarBackground(colorScheme.primary)

    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingLarge),
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.spacingLarge)
            .padding(bottom = Dimens.spacingLarge)
            .navigationBarsPadding()
    ) {
        Header()

        Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium)) {
            Section(
                iconRes = R.drawable.ic_keyboard,
                title = stringResource(R.string.setup_password_crack_time_info_patterns_title),
                body = stringResource(R.string.setup_password_crack_time_info_patterns_body),
            )
            Section(
                iconRes = R.drawable.ic_key,
                title = stringResource(R.string.setup_password_crack_time_info_attack_title),
                body = stringResource(
                    R.string.setup_password_crack_time_info_attack_body,
                    guessesPerSecond,
                ),
            )
        }

        TechnicalDetails(
            iterations = numberFormat.format(KekSpec.Password.kdfIterations),
            guessesPerSecond = guessesPerSecond,
        )

        Links()
    }
}

@Composable
private fun Header() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
        modifier = Modifier.fillMaxWidth(),
    ) {
////        Box(
////            contentAlignment = Alignment.Center,
////            modifier = Modifier
////                .size(64.dp)
////                .background(colorScheme.primaryContainer, CircleShape),
////        ) {
////            Icon(
////                painter = painterResource(R.drawable.ic_shield_check),
////                contentDescription = null,
////                tint = colorScheme.onPrimaryContainer,
////                modifier = Modifier.size(32.dp),
////            )
////        }
//
//        Spacer(Modifier.height(Dimens.spacingXSmall))

        Text(
            text = stringResource(R.string.setup_password_crack_time_info_title),
            style = typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Section(
    @DrawableRes iconRes: Int,
    title: String,
    body: String,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .background(colorScheme.secondaryContainer, CircleShape),
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingXxSmall)) {
            Text(
                text = title,
                style = typography.titleSmall,
            )
            Text(
                text = body,
                style = typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TechnicalDetails(
    iterations: String,
    guessesPerSecond: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)) {
        SectionLabel(stringResource(R.string.setup_password_crack_time_info_details_title))

        Surface(
            color = colorScheme.surfaceContainerHigh,
            shape = shapes.large,
        ) {
            Column(modifier = Modifier.padding(vertical = Dimens.spacingXSmall)) {
                DetailRow(
                    label = stringResource(R.string.setup_password_crack_time_info_kdf),
                    value = KekSpec.Password.kdf.value,
                )
                HorizontalDivider(
                    color = colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = Dimens.spacingLarge)
                )
                DetailRow(
                    label = stringResource(R.string.setup_password_crack_time_info_iterations),
                    value = iterations,
                )
                HorizontalDivider(
                    color = colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = Dimens.spacingLarge)
                )
                DetailRow(
                    label = stringResource(R.string.setup_password_crack_time_info_attack_speed),
                    value = stringResource(
                        R.string.setup_password_crack_time_info_attack_speed_value,
                        guessesPerSecond,
                    ),
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = Dimens.spacingLarge, vertical = Dimens.spacingSmall),
    ) {
        Text(
            text = label,
            style = typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun Links() {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)) {
        SectionLabel(stringResource(R.string.setup_password_crack_time_info_learn_more))

        Surface(
            color = colorScheme.surfaceContainerHigh,
            shape = shapes.large,
        ) {
            Column {
                LinkRow(
                    text = stringResource(R.string.setup_password_crack_time_info_link_zxcvbn4j),
                    onClick = { context.openUrl(ZXCVBN4J_URL) },
                )
                HorizontalDivider(
                    color = colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = Dimens.spacingLarge)
                )
                LinkRow(
                    text = stringResource(R.string.setup_password_crack_time_info_link_zxcvbn),
                    onClick = { context.openUrl(ZXCVBN_URL) },
                )
                HorizontalDivider(
                    color = colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = Dimens.spacingLarge)
                )
                LinkRow(
                    text = stringResource(R.string.setup_password_crack_time_info_link_encryption),
                    onClick = { context.openUrl(ENCRYPTION_URL) },
                )
            }
        }
    }
}

@Composable
private fun LinkRow(
    text: String,
    onClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Dimens.spacingLarge, vertical = Dimens.spacingSmall),
    ) {
        Text(
            text = text,
            style = typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Icon(
            painter = painterResource(R.drawable.ic_open_in_new),
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = typography.labelLarge,
        color = colorScheme.primary,
        modifier = Modifier.padding(horizontal = Dimens.spacingLarge),
    )
}

@Preview(heightDp = 800)
@Composable
private fun CrackTimeExplanationContentPreview() {
    AppTheme {
        Surface {
            CrackTimeExplanationContent()
        }
    }
}
