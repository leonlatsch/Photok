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

package dev.leonlatsch.photok.ui.systembars

import android.view.Window
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.core.view.WindowCompat

class SystemBarBackgroundRequest(color: Color) {
    var color by mutableStateOf(color)
}

class SystemBarsController {
    val statusBarRequests = mutableStateListOf<SystemBarBackgroundRequest>()
    val navigationBarRequests = mutableStateListOf<SystemBarBackgroundRequest>()
}

val LocalSystemBarsController = staticCompositionLocalOf { SystemBarsController() }

@Composable
fun ProvideSystemBarsController(window: Window, content: @Composable () -> Unit) {
    val controller = remember { SystemBarsController() }
    val defaultColor = colorScheme.background

    LaunchedEffect(controller, window, defaultColor) {
        snapshotFlow { controller.statusBarRequests.lastOrNull()?.color ?: defaultColor }
            .collect { color ->
                WindowCompat.getInsetsController(window, window.decorView)
                    .isAppearanceLightStatusBars = color.luminance() > 0.5f
            }
    }

    LaunchedEffect(controller, window, defaultColor) {
        snapshotFlow { controller.navigationBarRequests.lastOrNull()?.color ?: defaultColor }
            .collect { color ->
                WindowCompat.getInsetsController(window, window.decorView)
                    .isAppearanceLightNavigationBars = color.luminance() > 0.5f
            }
    }

    CompositionLocalProvider(LocalSystemBarsController provides controller, content = content)
}

@Composable
fun StatusBarBackground(color: Color) {
    val controller = LocalSystemBarsController.current
    val request = remember { SystemBarBackgroundRequest(color) }

    SideEffect { request.color = color }

    DisposableEffect(controller, request) {
        controller.statusBarRequests.add(request)
        onDispose { controller.statusBarRequests.remove(request) }
    }
}

@Composable
fun NavigationBarBackground(color: Color) {
    val controller = LocalSystemBarsController.current
    val request = remember { SystemBarBackgroundRequest(color) }

    SideEffect { request.color = color }

    DisposableEffect(controller, request) {
        controller.navigationBarRequests.add(request)
        onDispose { controller.navigationBarRequests.remove(request) }
    }
}
