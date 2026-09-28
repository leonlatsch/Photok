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


package dev.leonlatsch.photok.recoverymenu.ui

import androidx.activity.compose.LocalActivity
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.uicomponents.AppName

@Composable
fun RecoveryMenuScreen(
    viewModel: RecoveryMenuViewModel,
) {
    RecoveryMenuContent(
        handleUiEvent = viewModel::handleUiEvent,
    )
}

@Composable
private fun RecoveryMenuContent(
    handleUiEvent: (RecoveryMenuUiEvent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .background(
                Brush.verticalGradient(
                    0f to MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    0.5f to Color.Transparent,
                )
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                RecoveryMenuHeader()

                Spacer(modifier = Modifier.height(50.dp))

                RecoveryMenuActions(handleUiEvent = handleUiEvent)
            }

            Text(
                text = stringResource(R.string.common_copyright_notice),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun RecoveryMenuHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppName(fontSize = 50.sp)

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            Text(
                text = stringResource(R.string.recovery_subtitle).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun RecoveryMenuActions(
    handleUiEvent: (RecoveryMenuUiEvent) -> Unit,
) {
    val activity = LocalActivity.current

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        RecoveryMenuActionCard(
            title = stringResource(R.string.recovery_menu_item_open_photok),
            iconRes = R.drawable.ic_lock,
            onClick = { handleUiEvent(RecoveryMenuUiEvent.OpenPhotok(activity)) },
        )

        RecoveryMenuActionCard(
            title = stringResource(R.string.recovery_subtitle_reset_hide_app),
            iconRes = R.drawable.ic_eye,
            onClick = { handleUiEvent(RecoveryMenuUiEvent.ResetHideApp(activity)) },
        )
    }
}

@Composable
private fun RecoveryMenuActionCard(
    title: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(20.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun RecoveryMenuScreenPreview() {
    AppTheme {
        RecoveryMenuContent(
            handleUiEvent = {},
        )
    }
}
