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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.theme.AppTheme

@Composable
fun StatCard(
    label: String,
    icon: Int,
    stat: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(18.dp),
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = label,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Text(
                text = stat,
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    maxFontSize = MaterialTheme.typography.headlineMedium.fontSize,
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    AppTheme {
        Surface {
            Row(
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                modifier = Modifier.padding(20.dp)
            ) {
                StatCard(
                    label = "Photos",
                    icon = R.drawable.ic_image,
                    stat = "128",
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = "Failed",
                    icon = R.drawable.ic_warning,
                    stat = "2",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
