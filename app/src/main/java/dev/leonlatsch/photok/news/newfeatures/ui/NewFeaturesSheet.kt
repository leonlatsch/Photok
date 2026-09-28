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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.other.openUrl
import dev.leonlatsch.photok.settings.ui.compose.LocalConfig
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.uicomponents.AppName
import dev.leonlatsch.photok.ui.uicomponents.ShimmerProBadge
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * The one big thing of this release. Shown as a large card on top of the sheet.
 * [points] are optional short bullet points rendered inside the card.
 */
data class ReleaseHighlight(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    @StringRes val summary: Int,
    val points: List<Int> = emptyList(),
)

enum class ChangeKind(@StringRes val label: Int) {
    New(R.string.news_change_kind_new),
    Improved(R.string.news_change_kind_improved),
    Fixed(R.string.news_change_kind_fixed),
}

/**
 * Additional changes of this release. Listed below the [ReleaseHighlight].
 * Set [isPro] to mark a change as a pro feature.
 */
enum class ReleaseChange(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    @StringRes val summary: Int,
    val kind: ChangeKind,
    val isPro: Boolean = false,
) {
    Change1(
        icon = R.drawable.ic_password,
        title = R.string.release_change_title_1,
        summary = R.string.release_change_summary_1,
        kind = ChangeKind.Fixed,
    ),
    Change2(
        icon = R.drawable.ic_check_circle_outline,
        title = R.string.release_change_title_2,
        summary = R.string.release_change_summary_2,
        kind = ChangeKind.Improved,
    ),
}

val releaseHighlight = ReleaseHighlight(
    icon = R.drawable.ic_backup_restore,
    title = R.string.release_highlight_title,
    summary = R.string.release_highlight_summary,
    points = listOf(
        R.string.release_highlight_point_1,
        R.string.release_highlight_point_2,
        R.string.release_highlight_point_3,
        R.string.release_highlight_point_4,
    ),
)

/**
 * Increase for this Dialog to show on the next update.
 * @see dev.leonlatsch.photok.gallery.ui.GalleryViewModel.runIfNews
 */
const val FEATURE_VERSION_CODE = 15

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewFeaturesSheet(overrideShow: Boolean = false, onDismissOverride: () -> Unit = {}) {
    val config = LocalConfig.current

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        config ?: return@LaunchedEffect

        if (config.systemLastFeatureVersionCode < FEATURE_VERSION_CODE) {
            delay(300.milliseconds)
            visible = true
            config.systemLastFeatureVersionCode = FEATURE_VERSION_CODE
        }
    }

    if (visible || overrideShow) {
        val state = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )

        ModalBottomSheet(
            sheetState = state,
            onDismissRequest = {
                visible = false
                onDismissOverride()
            },
            dragHandle = null,
            sheetGesturesEnabled = false,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            contentWindowInsets = { WindowInsets() }
        ) {
            val scope = rememberCoroutineScope()

            NewFeaturesContent(
                onContinueClick = {
                    scope.launch {
                        state.hide()
                    }.invokeOnCompletion {
                        visible = false
                        onDismissOverride()
                    }
                },
                modifier = Modifier.statusBarsPadding(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewFeaturesContent(
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = BottomSheetDefaults.ExpandedShape,
        color = BottomSheetDefaults.ContainerColor,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 100.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                NewFeaturesHeader(
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                HighlightCard(
                    highlight = releaseHighlight,
                    modifier = Modifier.entrance(index = 0),
                )

                Spacer(modifier = Modifier.height(24.dp))

                ReleaseChange.entries.forEachIndexed { index, change ->
                    ReleaseChangeRow(
                        change = change,
                        modifier = Modifier.entrance(index = index + 1),
                    )
                    Spacer(modifier = Modifier.height(20.dp))
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

                Button(
                    onClick = onContinueClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.common_continue)
                    )
                }
            }
        }
    }
}

@Composable
private fun NewFeaturesHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppName(
            fontSize = 48.sp
        )
        Text(
            text = "${stringResource(R.string.news_new_in_title)} ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun HighlightCard(
    highlight: ReleaseHighlight,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(colors.primaryContainer, colors.tertiaryContainer),
                )
            )
    ) {
        Icon(
            painter = painterResource(highlight.icon),
            contentDescription = null,
            tint = colors.onPrimaryContainer.copy(alpha = 0.08f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(180.dp)
                .offset(x = 50.dp, y = (-30).dp)
                .rotate(-18f)
        )

        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Text(
                text = stringResource(highlight.title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onPrimaryContainer,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(highlight.summary),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onPrimaryContainer.copy(alpha = 0.8f),
            )

            if (highlight.points.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for (point in highlight.points) {
                        HighlightPoint(text = stringResource(point))
                    }
                }
            }
        }
    }
}

@Composable
private fun HighlightPoint(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_check),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun ReleaseChangeRow(
    change: ReleaseChange,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Icon(
                painter = painterResource(change.icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(10.dp)
                    .size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ChangeKindPill(kind = change.kind)
                if (change.isPro) {
                    ShimmerProBadge(textStyle = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(change.title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(change.summary),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ChangeKindPill(kind: ChangeKind) {
    val colors = MaterialTheme.colorScheme
    when (kind) {
        ChangeKind.New -> Pill(
            text = stringResource(kind.label),
            containerColor = colors.primaryContainer,
            contentColor = colors.onPrimaryContainer,
        )

        ChangeKind.Improved -> Pill(
            text = stringResource(kind.label),
            containerColor = colors.tertiaryContainer,
            contentColor = colors.onTertiaryContainer,
        )

        ChangeKind.Fixed -> Pill(
            text = stringResource(kind.label),
            containerColor = colors.secondaryContainer,
            contentColor = colors.onSecondaryContainer,
        )
    }
}

@Composable
private fun Pill(
    text: String,
    containerColor: Color,
    contentColor: Color,
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Black,
        color = contentColor,
        modifier = Modifier
            .clip(CircleShape)
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

/**
 * Fades and slides content in, staggered by [index].
 */
@Composable
private fun Modifier.entrance(index: Int): Modifier {
    if (LocalInspectionMode.current) return this

    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay((150L + index * 80L).milliseconds)
        progress.animateTo(1f, animationSpec = tween(durationMillis = 400))
    }

    return graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 24.dp.toPx()
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    AppTheme {
        NewFeaturesContent(onContinueClick = {})
    }
}
