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

import android.icu.text.CompactDecimalFormat
import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.icu.util.ULocale
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.models.PasswordStrength
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.theme.Dimens
import java.util.Locale
import kotlin.math.roundToLong

private const val SEGMENT_COUNT = 5
private const val STRONG_SCORE = 3

private const val MINUTE = 60.0
private const val HOUR = MINUTE * 60
private const val DAY = HOUR * 24
private const val MONTH = DAY * 31
private const val YEAR = MONTH * 12
private const val CENTURY = YEAR * 100
private const val AGE_OF_UNIVERSE = YEAR * 13.8e9

@Composable
fun PasswordStrengthIndicator(
    strength: PasswordStrength,
    showFeedback: Boolean,
    modifier: Modifier = Modifier,
) {
    val levelColor by animateColorAsState(
        targetValue = strengthColor(strength.score),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
    )
    val locale = LocalConfiguration.current.locales[0]
    val crackTime = remember(strength.crackTimeSeconds, locale) {
        formatCrackTime(strength.crackTimeSeconds, locale)
    }
    val crackTimeText = if (strength.crackTimeSeconds > AGE_OF_UNIVERSE) {
        stringResource(R.string.setup_password_strength_crack_time_universe)
    } else {
        stringResource(R.string.setup_password_strength_crack_time, crackTime)
    }

    Surface(
        tonalElevation = 5.dp,
        shape = shapes.medium,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
            modifier = Modifier
                .animateContentSize()
                .padding(Dimens.spacingMedium),
        ) {
            Header(
                strengthLabel = strengthLabel(strength.score),
                levelColor = levelColor,
                crackTimeText = crackTimeText,
            )

            StrengthBar(
                filledSegments = strength.score + 1,
                color = levelColor,
            )

            if (showFeedback && strength.score < STRONG_SCORE) {
                strength.warning?.let { warning ->
                    FeedbackLine(
                        text = warning,
                        iconRes = R.drawable.ic_warning,
                        iconTint = levelColor,
                        textColor = colorScheme.onSurface,
                    )
                }

                strength.suggestions.firstOrNull()?.let { suggestion ->
                    FeedbackLine(
                        text = suggestion,
                        iconRes = R.drawable.ic_info,
                        iconTint = colorScheme.onSurfaceVariant,
                        textColor = colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(
    strengthLabel: String,
    levelColor: Color,
    crackTimeText: String,
) {
    var showExplanation by rememberSaveable { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXSmall),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.SpaceBetween,
            itemVerticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .padding(top = Dimens.spacingXxSmall),
        ) {
            Text(
                text = strengthLabel,
                style = typography.labelLarge,
                color = levelColor,
                modifier = Modifier.padding(end = Dimens.spacingSmall),
            )
            Text(
                text = crackTimeText,
                style = typography.labelMedium,
                color = colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_info),
            contentDescription = stringResource(R.string.setup_password_crack_time_info_title),
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(role = Role.Button) { showExplanation = true }
                .padding(Dimens.spacingXxSmall)
                .size(18.dp),
        )
    }

    if (showExplanation) {
        CrackTimeExplanationSheet(onDismissRequest = { showExplanation = false })
    }
}

@Composable
private fun StrengthBar(
    filledSegments: Int,
    color: Color,
) {
    val targetProgress by animateFloatAsState(
        targetValue = (filledSegments / SEGMENT_COUNT.toFloat()),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow,
        )
    )

    LinearProgressIndicator(
        progress = { targetProgress },
        color = color,
        trackColor = colorScheme.surfaceContainerHighest,
        drawStopIndicator = {},
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
    )
}

@Composable
private fun FeedbackLine(
    text: String,
    iconRes: Int,
    iconTint: Color,
    textColor: Color,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier
                .padding(top = Dimens.spacingXxSmall)
                .size(16.dp),
        )
        Text(
            text = text,
            style = typography.bodySmall,
            color = textColor,
        )
    }
}

@Composable
private fun strengthColor(score: Int): Color = when (score) {
    0 -> colorScheme.error
    1 -> Color(0xFFE8710A)
    2 -> Color(0xFFD9A400)
    else -> colorScheme.primary
}

private fun formatCrackTime(seconds: Double, locale: Locale): String {
    val format = MeasureFormat.getInstance(locale, MeasureFormat.FormatWidth.WIDE)
    return when {
        seconds < 1 -> "< " + format.format(Measure(1, MeasureUnit.SECOND))
        seconds < MINUTE -> format.format(Measure(seconds.roundToLong(), MeasureUnit.SECOND))
        seconds < HOUR -> format.format(
            Measure(
                (seconds / MINUTE).roundToLong(),
                MeasureUnit.MINUTE
            )
        )

        seconds < DAY -> format.format(Measure((seconds / HOUR).roundToLong(), MeasureUnit.HOUR))
        seconds < MONTH -> format.format(Measure((seconds / DAY).roundToLong(), MeasureUnit.DAY))
        seconds < YEAR -> format.format(Measure((seconds / MONTH).roundToLong(), MeasureUnit.MONTH))
        seconds < CENTURY -> format.format(
            Measure(
                (seconds / YEAR).roundToLong(),
                MeasureUnit.YEAR
            )
        )

        else -> {
            val compactNumbers =
                CompactDecimalFormat.getInstance(locale, CompactDecimalFormat.CompactStyle.LONG)
            MeasureFormat.getInstance(
                ULocale.forLocale(locale),
                MeasureFormat.FormatWidth.WIDE,
                compactNumbers
            )
                .format(Measure((seconds / CENTURY).roundToLong(), MeasureUnit.CENTURY))
        }
    }
}

@Composable
private fun strengthLabel(score: Int): String = when (score) {
    0 -> stringResource(R.string.setup_password_strength_very_weak)
    1 -> stringResource(R.string.setup_password_strength_weak)
    2 -> stringResource(R.string.setup_password_strength_moderate)
    3 -> stringResource(R.string.setup_password_strength_strong)
    else -> stringResource(R.string.setup_password_strength_very_strong)
}

@PreviewLightDark
@Composable
private fun PasswordStrengthIndicatorWeakPreview() {
    AppTheme {
        PasswordStrengthIndicator(
            strength = PasswordStrength(
                score = 1,
                crackTimeSeconds = 3 * HOUR,
                warning = "This is similar to a commonly used password.",
                suggestions = listOf(
                    "Add another word or two. Uncommon words are better.",
                    "Capitalization doesn't help very much.",
                ),
            ),
            showFeedback = true,
            modifier = Modifier.padding(Dimens.spacingLarge),
        )
    }
}

@PreviewLightDark
@Composable
private fun PasswordStrengthIndicatorStrongPreview() {
    AppTheme {
        PasswordStrengthIndicator(
            strength = PasswordStrength(
                score = 4,
                crackTimeSeconds = 1e20,
                warning = null,
                suggestions = emptyList(),
            ),
            showFeedback = true,
            modifier = Modifier.padding(Dimens.spacingLarge),
        )
    }
}
