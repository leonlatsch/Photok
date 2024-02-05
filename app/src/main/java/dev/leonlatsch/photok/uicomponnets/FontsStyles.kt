/*
 *   Copyright 2020-2023 Leon Latsch
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

package dev.leonlatsch.photok.uicomponnets

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import dev.leonlatsch.photok.R

private val FontFamily = FontFamily(
    Font(
        weight = FontWeight.Light,
        resId = R.font.nunito_sans_light,
    ),
    Font(
        weight = FontWeight.Normal,
        style = FontStyle.Normal,
        resId = R.font.nunito_sans_regular,
    ),
    Font(
        weight = FontWeight.Normal,
        style = FontStyle.Normal,
        resId = R.font.nunito_sans_italic,
    ),
    Font(
        weight = FontWeight.SemiBold,
        style = FontStyle.Normal,
        resId = R.font.nunito_sans_semi_bold,
    ),
    Font(
        weight = FontWeight.Bold,
        style = FontStyle.Normal,
        resId = R.font.nunito_sans_bold,
    ),
    Font(
        weight = FontWeight.Bold,
        style = FontStyle.Italic,
        resId = R.font.nunito_sans_bold_italic,
    ),
    Font(
        weight = FontWeight.Black,
        style = FontStyle.Normal,
        resId = R.font.nunito_sans_black,
    ),
    Font(
        weight = FontWeight.Black,
        style = FontStyle.Italic,
        resId = R.font.nunito_sans_black_italic,
    ),
)