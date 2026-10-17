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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.models.PasswordStrength
import dev.leonlatsch.photok.ui.theme.AppTheme
import dev.leonlatsch.photok.ui.theme.Dimens
import java.util.Locale
import kotlin.math.roundToLong

private const val SEGMENT_COUNT = 5

private const val MINUTE = 60.0
private const val HOUR = MINUTE * 60
private const val DAY = HOUR * 24
private const val MONTH = DAY * 31
private const val YEAR = MONTH * 12
private const val CENTURY = YEAR * 100

@Composable
fun PasswordStrengthIndicator(
    strength: PasswordStrength,
    modifier: Modifier = Modifier,
) {
    val levelColor by animateColorAsState(strengthColor(strength.score))
    val locale = LocalConfiguration.current.locales[0]
    val crackTime = remember(strength.crackTimeSeconds, locale) {
        formatCrackTime(strength.crackTimeSeconds, locale)
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.setup_password_strength_label),
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = strengthLabel(strength.score),
                    style = typography.labelLarge,
                    color = levelColor,
                )
            }

            StrengthBar(
                filledSegments = strength.score + 1,
                color = levelColor,
            )

            Text(
                text = stringResource(R.string.setup_password_strength_crack_time, crackTime),
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
            )

            strength.warning?.let { warning ->
                FeedbackLine(
                    text = warning,
                    iconRes = R.drawable.ic_warning,
                    iconTint = levelColor,
                    textColor = colorScheme.onSurface,
                )
            }

            strength.suggestions.forEach { suggestion ->
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

@Composable
private fun StrengthBar(
    filledSegments: Int,
    color: Color,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXSmall),
        modifier = Modifier.fillMaxWidth(),
    ) {
        repeat(SEGMENT_COUNT) { index ->
            val segmentColor by animateColorAsState(
                if (index < filledSegments) color else colorScheme.surfaceContainerHighest
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(segmentColor)
            )
        }
    }
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
        seconds < HOUR -> format.format(Measure((seconds / MINUTE).roundToLong(), MeasureUnit.MINUTE))
        seconds < DAY -> format.format(Measure((seconds / HOUR).roundToLong(), MeasureUnit.HOUR))
        seconds < MONTH -> format.format(Measure((seconds / DAY).roundToLong(), MeasureUnit.DAY))
        seconds < YEAR -> format.format(Measure((seconds / MONTH).roundToLong(), MeasureUnit.MONTH))
        seconds < CENTURY -> format.format(Measure((seconds / YEAR).roundToLong(), MeasureUnit.YEAR))
        else -> {
            val compactNumbers = CompactDecimalFormat.getInstance(locale, CompactDecimalFormat.CompactStyle.LONG)
            MeasureFormat.getInstance(ULocale.forLocale(locale), MeasureFormat.FormatWidth.WIDE, compactNumbers)
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
            modifier = Modifier.padding(Dimens.spacingLarge),
        )
    }
}
