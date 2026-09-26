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

package dev.leonlatsch.photok.backup.ui.restore

import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import java.util.Locale

/**
 * Formats [millis] as a duration in the units, abbreviations and order of the current locale,
 * e.g. "1 min 23 sec", "1 Min. 23 Sek." or "1分23秒".
 *
 * Seconds are dropped once the duration passes an hour, they are noise at that scale.
 */
fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val measureFormat = MeasureFormat.getInstance(
        Locale.getDefault(),
        MeasureFormat.FormatWidth.SHORT,
    )

    return when {
        hours > 0 -> measureFormat.formatMeasures(
            Measure(hours, MeasureUnit.HOUR),
            Measure(minutes, MeasureUnit.MINUTE),
        )

        minutes > 0 -> measureFormat.formatMeasures(
            Measure(minutes, MeasureUnit.MINUTE),
            Measure(seconds, MeasureUnit.SECOND),
        )

        else -> measureFormat.formatMeasures(Measure(seconds, MeasureUnit.SECOND))
    }
}
