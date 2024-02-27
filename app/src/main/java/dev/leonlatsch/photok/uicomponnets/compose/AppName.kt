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

package dev.leonlatsch.photok.uicomponnets.compose

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import dev.leonlatsch.photok.R

@Composable
fun AppName(
    color: Color = colorResource(R.color.appTitleColor),
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.app_name),
        color = color,
        fontFamily = FontFamily(Font(R.font.lobster_regular)),
        fontSize = 38.sp,
        modifier = modifier
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
private fun AppNamePreview() {
    AppName()
}