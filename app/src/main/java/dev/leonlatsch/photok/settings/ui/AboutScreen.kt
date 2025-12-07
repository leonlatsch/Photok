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

package dev.leonlatsch.photok.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.news.newfeatures.ui.NewFeaturesDialog
import dev.leonlatsch.photok.other.extensions.show
import dev.leonlatsch.photok.other.openUrl
import dev.leonlatsch.photok.ui.LocalFragment
import dev.leonlatsch.photok.ui.components.AppName
import dev.leonlatsch.photok.ui.theme.AppTheme

sealed interface AboutUiEvent {
    data object Close : AboutUiEvent
    data object OpenThirdParty : AboutUiEvent
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    handleUiEvent: (AboutUiEvent) -> Unit,
) {
    val context = LocalContext.current
    val fragment = LocalFragment.current

    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.settings_other_about_title))
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                handleUiEvent(AboutUiEvent.Close)
                            },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_back),
                                contentDescription = stringResource(R.string.process_close),
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                fragment?.let {
                                    NewFeaturesDialog().show(it.childFragmentManager)
                                }
                            }
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_outline_campaign),
                                contentDescription = stringResource(R.string.news_new_in_title),
                            )
                        }
                    }
                )
            },
            bottomBar = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Text(
                        text = stringResource(R.string.common_copyright_notice),
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        val privacyUrl = stringResource(R.string.about_privacy_policy_url)

                        Text(
                            text = stringResource(R.string.about_third_party),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.MiddleEllipsis,
                            modifier = Modifier.clickable(role = Role.Button) {
                                handleUiEvent(AboutUiEvent.OpenThirdParty)
                            }
                        )
                        Text(text = "|")
                        Text(
                            text = stringResource(R.string.about_privacy_policy),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.MiddleEllipsis,
                            modifier = Modifier.clickable(role = Role.Button) {
                                context.openUrl(privacyUrl)
                            }
                        )
                    }
                }
            }
        ) { contentPadding ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding)
                    .fillMaxSize()
            ) {
                AppName(
                    fontSize = 62.sp,
                    modifier = Modifier
                        .padding(top = 80.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = stringResource(R.string.about_version))
                    Text(BuildConfig.VERSION_NAME)
                }

                Text(
                    text = stringResource(R.string.about_developed_by),
                    modifier = Modifier
                        .padding(top = 80.dp)
                )

                val websiteUrl = stringResource(R.string.about_website_url)

                TextButton(
                    onClick = {
                        context.openUrl(websiteUrl)
                    },
                ) {
                    Text(
                        text = stringResource(R.string.about_website_label),
                        fontSize = 36.sp,
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    AboutScreen(
        handleUiEvent = {},
    )
}