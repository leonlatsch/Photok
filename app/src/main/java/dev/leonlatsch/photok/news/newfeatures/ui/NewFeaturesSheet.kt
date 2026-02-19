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

package dev.leonlatsch.photok.news.newfeatures.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.other.openUrl
import dev.leonlatsch.photok.settings.ui.compose.LocalConfig
import dev.leonlatsch.photok.ui.components.AppName
import dev.leonlatsch.photok.ui.theme.AppTheme
import kotlinx.coroutines.launch


data class NewFeature(
    val emoji: String,
    val title: String,
    val summary: String
)
private val NewFeatures = listOf(
    NewFeature(
        emoji = "\uD83D\uDDBC\uFE0F",
        title = "Brand New Image Viewer",
        summary = "A rebuilt image viewer and details sheet for a much smoother browsing experience.",
    ),
    NewFeature(
        emoji = "\uD83C\uDFAC",
        title = "Whole New Video Player",
        summary = "A redesigned player offering seamless playback with new mute, loop, and zoom features.",
    ),
    NewFeature(
        emoji = "⚡",
        title = "Lightning-Fast Video Loading",
        summary = "Incredibly fast video buffering and skipping for an uninterrupted viewing experience.",
    ),
    NewFeature(
        emoji = "✨",
        title = "Crisper, Clearer Thumbnails",
        summary = "Sharper, higher-resolution thumbnails perfectly optimized to keep file sizes small.",
    ),
    NewFeature(
        emoji = "\uD83D\uDCC1",
        title = "Quick Add to Albums",
        summary = "Easily add photos directly to your albums right from the image viewer.",
    ),
)

/**
 * Increase for this Dialog to show on the next update.
 * @see dev.leonlatsch.photok.gallery.ui.GalleryViewModel.runIfNews
 */
const val FEATURE_VERSION_CODE = 12

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewFeaturesSheet() {
    val config = LocalConfig.current

    val isPreview = LocalInspectionMode.current
    var visible by remember { mutableStateOf(isPreview) }

    LaunchedEffect(Unit) {
        config ?: return@LaunchedEffect

        if (config.systemLastFeatureVersionCode < FEATURE_VERSION_CODE) {
            visible = true
//            config.systemLastFeatureVersionCode = FEATURE_VERSION_CODE
        }
    }

    if (visible) {
        val state = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )

        ModalBottomSheet(
            sheetState = state,
            onDismissRequest = { visible = false },
            dragHandle = null,
            sheetGesturesEnabled = false,
            containerColor = Color.Transparent
        ) {
            Surface(
                shape = BottomSheetDefaults.ExpandedShape,
                color = BottomSheetDefaults.ContainerColor,
            ) {
                Box {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 100.dp)
                    ) {
                        Image(
                            painterResource(R.drawable.app_icon),
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                        )

                        AppName()

                        Text(
                            text = BuildConfig.VERSION_NAME,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        for (feature in NewFeatures) {
                            NewFeatureRow(
                                feature = feature,
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .background(BottomSheetDefaults.ContainerColor)
                            .padding(horizontal = 20.dp)
                    ) {
                        val context = LocalContext.current
                        val changelogUrl = stringResource(R.string.news_changelog_url)

                        TextButton(
                            onClick = { context.openUrl(changelogUrl) }
                        ) {
                            Text(
                                text = stringResource(R.string.news_view_changelog)
                            )
                        }

                        val scope = rememberCoroutineScope()

                        Button(
                            onClick = {
                                scope.launch {
                                    state.hide()
                                }.invokeOnCompletion {
                                    visible = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.common_ok)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewFeatureRow(
    feature: NewFeature,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = feature.emoji,
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column() {
            Text(
                text = feature.title,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = feature.summary,
                color = MaterialTheme.colorScheme.outline,
                fontSize = 15.sp,
            )
        }
    }

}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(showSystemUi = true)
@Preview(showSystemUi = true, device = Devices.NEXUS_5)
@PreviewScreenSizes
@Composable
private fun Preview() {
    AppTheme() {
        NewFeaturesSheet()
    }
}