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

package dev.leonlatsch.photok.news.newfeatures.ui.model

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.components.AppName
import dev.leonlatsch.photok.ui.theme.AppTheme


data class NewFeature(
    val emoji: String,
    val title: String,
    val summary: String
)

private val NewFeatures = listOf(
    NewFeature(
        emoji = "\uD83D\uDDBC\uFE0F",
        title = "Brand New Image Viewer",
        summary = "Enjoy a smoother browsing experience. We've completely rebuilt the image viewer and the details sheet from the ground up for better performance.",
    ),
    NewFeature(
        emoji = "\uD83C\uDFAC",
        title = "Upgraded Video Player",
        summary = "Watch videos seamlessly with our completely redesigned player. We've added highly requested features, including the ability to mute, loop, and zoom in on videos.",
    ),
    NewFeature(
        emoji = "⚡",
        title = "Lightning-Fast Video Loading",
        summary = "Say goodbye to waiting. Video buffering and skipping are now incredibly fast, giving you a vastly improved and uninterrupted viewing experience.",
    ),
    NewFeature(
        emoji = "✨",
        title = "Crisper, Clearer Thumbnails",
        summary = "Thumbnails now look significantly sharper. We've upgraded the resolution while keeping file sizes perfectly optimized, so you get amazing visual quality without taking up extra space.",
    ),
    NewFeature(
        emoji = "\uD83D\uDCC1",
        title = "Quick Add to Albums",
        summary = "Organizing your media just got easier. You can now add photos directly to your albums right from the image viewer.",
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewFeaturesSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
) {
    if (visible) {
        val state = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )

        ModalBottomSheet(
            sheetState = state,
            onDismissRequest = onDismissRequest,
            dragHandle = null,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_party_popper),
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .graphicsLayer {
                                this.rotationY = 180f
                            }

                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.news_new_in_title)
                        )
                        AppName()

                        Text(
                            text = BuildConfig.VERSION_NAME
                        )
                    }

                    Image(
                        painter = painterResource(R.drawable.ic_party_popper),
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                for (feature in NewFeatures) {
                    NewFeatureRow(
                        feature = feature,
                    )

                    if (feature != NewFeatures.last()) {
                        Spacer(modifier = Modifier.height(10.dp))
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
        modifier = modifier
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
@PreviewLightDark
@Composable
private fun Preview() {
    AppTheme() {
        Scaffold() {
            NewFeaturesSheet(
                visible = true,
                onDismissRequest = {},
            )
        }
    }
}