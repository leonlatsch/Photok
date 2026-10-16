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

package dev.leonlatsch.photok.onboarding.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.navigation.LocalNavigator
import dev.leonlatsch.photok.onboarding.ui.OnboardingRoute
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingNexButtonBar
import dev.leonlatsch.photok.onboarding.ui.components.OnboardingPageSubtitle
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.theme.Dimens

private enum class StartOption { Fresh, Restore }

@Composable
fun OnboardingSelectStartScreen() {
    val navigator = LocalNavigator.current
    var selectedOption by rememberSaveable { mutableStateOf(StartOption.Fresh) }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            OnboardingNexButtonBar {
                navigator.navigate(OnboardingRoute.SetupPassword)
            }
        }
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(Dimens.spacingLarge)
        ) {
            OnboardingPageSubtitle(
                text = "You can always import a backup later."
            )

            Spacer(Modifier.height(Dimens.spacingXSmall))

            StartOptionCard(
                icon = R.drawable.ic_add,
                title = "New Vault",
                summary = "Create a new, empty vault",
                selected = selectedOption == StartOption.Fresh,
                onClick = { selectedOption = StartOption.Fresh },
            )

            StartOptionCard(
                icon = R.drawable.ic_backup_restore,
                title = "Restore a backup",
                summary = "Restore an existing Photok backup",
                selected = selectedOption == StartOption.Restore,
                onClick = { selectedOption = StartOption.Restore },
            )
        }
    }

}

@Composable
private fun StartOptionCard(
    @DrawableRes icon: Int,
    title: String,
    summary: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    )
    val elevation by animateDpAsState(
        if (selected) 20.dp else 2.dp
    )

    Surface(
        selected = selected,
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = elevation,
        border = BorderStroke(1.5.dp, borderColor),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
            modifier = Modifier.padding(Dimens.spacingMedium),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(24.dp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            RadioButton(
                selected = selected,
                onClick = null,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun OnboardingSelectStartScreenPreview() {
    AppTheme {
        OnboardingSelectStartScreen()
    }
}
