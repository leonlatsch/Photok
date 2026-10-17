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

package dev.leonlatsch.photok.ui.components

import android.os.Build
import android.view.RoundedCorner
import android.view.WindowInsets
import androidx.annotation.RequiresApi
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class DeviceCornerRadii(
    val topLeft: Dp,
    val topRight: Dp,
    val bottomRight: Dp,
    val bottomLeft: Dp,
)

@Composable
fun rememberDeviceCornerRadii(): DeviceCornerRadii {
    val view = LocalView.current
    val density = LocalDensity.current
    val orientation = LocalConfiguration.current.orientation

    return remember(view, density, orientation) {
        val insets = view.rootWindowInsets
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || insets == null) {
            return@remember DeviceCornerRadii(0.dp, 0.dp, 0.dp, 0.dp)
        }

        DeviceCornerRadii(
            topLeft = insets.cornerRadius(RoundedCorner.POSITION_TOP_LEFT, density),
            topRight = insets.cornerRadius(RoundedCorner.POSITION_TOP_RIGHT, density),
            bottomRight = insets.cornerRadius(RoundedCorner.POSITION_BOTTOM_RIGHT, density),
            bottomLeft = insets.cornerRadius(RoundedCorner.POSITION_BOTTOM_LEFT, density),
        )
    }
}

@Composable
fun rememberDeviceCornerShape(): Shape {
    val radii = rememberDeviceCornerRadii()

    return remember(radii) {
        AbsoluteRoundedCornerShape(
            topLeft = radii.topLeft,
            topRight = radii.topRight,
            bottomRight = radii.bottomRight,
            bottomLeft = radii.bottomLeft,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.S)
private fun WindowInsets.cornerRadius(position: Int, density: Density): Dp {
    val radius = getRoundedCorner(position)?.radius ?: 0
    return with(density) { radius.toDp() }
}
