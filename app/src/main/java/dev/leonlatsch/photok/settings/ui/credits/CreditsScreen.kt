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

package dev.leonlatsch.photok.settings.ui.credits

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.other.openUrl
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun CreditsScreen(
    onClose: () -> Unit,
    viewModel: CreditsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    AppTheme {
        CreditsContent(
            uiState = uiState,
            handleUiEvent = { event ->
                when (event) {
                    CreditsUiEvent.Close -> onClose()
                    is CreditsUiEvent.OpenWebsite -> context.openUrl(event.url)
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreditsContent(
    uiState: CreditsUiState,
    handleUiEvent: (CreditsUiEvent) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.settings_other_credits_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = { handleUiEvent(CreditsUiEvent.Close) },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = stringResource(R.string.process_close),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(vertical = 20.dp)
                .navigationBarsPadding()
        ) {
            ContributorsSection(
                contributors = uiState.contributors,
                handleUiEvent = handleUiEvent,
            )

            IconCreditsSection(iconCreditsHtml = uiState.iconCreditsHtml)
        }
    }
}

@Composable
private fun ContributorsSection(
    contributors: List<Contributor>,
    handleUiEvent: (CreditsUiEvent) -> Unit,
) {
    CreditsSection(
        title = stringResource(R.string.credits_contributors_title),
        summary = stringResource(R.string.credits_contributors_hint),
    ) {
        contributors.forEachIndexed { index, contributor ->
            val shape = when {
                contributors.size == 1 -> RoundedCornerShape(18.dp)
                index == 0 -> RoundedCornerShape(18.dp, 18.dp, 6.dp, 6.dp)
                index == contributors.lastIndex -> RoundedCornerShape(6.dp, 6.dp, 18.dp, 18.dp)
                else -> RoundedCornerShape(6.dp)
            }

            Surface(
                shape = shape,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.padding(bottom = 2.dp),
            ) {
                ContributorView(
                    contributor = contributor,
                    onClick = {
                        handleUiEvent(CreditsUiEvent.OpenWebsite(contributor.websiteUrl))
                    },
                )
            }
        }
    }
}

@Composable
private fun IconCreditsSection(iconCreditsHtml: String) {
    CreditsSection(
        title = stringResource(R.string.credits_icons_title),
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Text(
                text = AnnotatedString.fromHtml(
                    htmlString = iconCreditsHtml,
                    linkStyles = TextLinkStyles(
                        style = SpanStyle(color = MaterialTheme.colorScheme.primary)
                    ),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp)
            )
        }
    }
}

/**
 * Section of the credits screen. Styled like the sections on the settings screen,
 * with an illustration on top.
 */
@Composable
private fun CreditsSection(
    title: String,
    summary: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 30.dp)
        )

        if (summary != null) {
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 30.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier.padding(horizontal = 15.dp),
            content = content,
        )
    }
}

@Composable
private fun ContributorView(
    contributor: Contributor,
    onClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        modifier = Modifier
            .clickable(role = Role.Button, onClick = onClick)
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_person),
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = contributor.name,
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                text = contributor.contribution,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            ContributorDetail(
                icon = R.drawable.ic_email,
                text = contributor.contact,
                color = MaterialTheme.colorScheme.outline,
            )

            ContributorDetail(
                icon = R.drawable.ic_web,
                text = contributor.website,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

@Composable
private fun ContributorDetail(
    @DrawableRes icon: Int,
    text: String,
    color: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
        )
    }
}

@PreviewLightDark
@Composable
private fun CreditsScreenPreview() {
    AppTheme {
        CreditsContent(
            uiState = CreditsUiState(
                contributors = listOf(
                    Contributor(
                        name = "Leon Latsch",
                        contribution = "Main programming and designing, maintaining of the project",
                        contact = "hello@leonlatsch.dev",
                        website = "leonlatsch.dev",
                        websiteUrl = "https://leonlatsch.dev",
                    ),
                    Contributor(
                        name = "Some Translator",
                        contribution = "Maintaining spanish translation",
                        contact = "translator@example.com",
                        website = "github.com/translator",
                        websiteUrl = "https://github.com/translator",
                    ),
                ),
                iconCreditsHtml = "<div>Icons made by <a href=\"https://www.flaticon.com\">Flaticon</a></div>",
            ),
            handleUiEvent = {},
        )
    }
}
