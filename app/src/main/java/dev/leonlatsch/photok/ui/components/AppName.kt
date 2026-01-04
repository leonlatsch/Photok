


package dev.leonlatsch.photok.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import dev.leonlatsch.photok.R

@Composable
fun AppName(
    color: Color = colorResource(R.color.appTitleColor),
    fontSize: TextUnit = 38.sp,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.app_name),
        color = color,
        fontFamily = FontFamily(Font(R.font.lobster_regular)),
        fontSize = fontSize,
        modifier = modifier
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
private fun AppNamePreview() {
    AppName()
}

package dev.leonlatsch.photok.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import dev.leonlatsch.photok.R

@Composable
fun AppName(
    color: Color = colorResource(R.color.appTitleColor),
    fontSize: TextUnit = 38.sp,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.app_name),
        color = color,
        fontFamily = FontFamily(Font(R.font.lobster_regular)),
        fontSize = fontSize,
        modifier = modifier
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
private fun AppNamePreview() {
    AppName()
}