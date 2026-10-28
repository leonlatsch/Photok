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

package dev.leonlatsch.photok.main.ui.navigation

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

internal val LocalResetExitConfirmation = staticCompositionLocalOf<() -> Unit> { {} }

internal class ExitConfirmation(
    private val elapsedRealtime: () -> Long = SystemClock::elapsedRealtime,
) {
    private var lastBackPress: Long? = null

    fun onBack(): Boolean {
        val now = elapsedRealtime()
        val previous = lastBackPress
        if (previous != null && now - previous in 0L until 2_000L) {
            reset()
            return true
        }
        lastBackPress = now
        return false
    }

    fun reset() {
        lastBackPress = null
    }
}

@Composable
internal fun ExitConfirmationBackHandler(
    enabled: Boolean,
    confirmation: ExitConfirmation,
    onShowHint: () -> Unit,
    onExit: () -> Unit,
) {
    val orientation = LocalConfiguration.current.orientation
    DisposableEffect(enabled, confirmation, orientation) {
        confirmation.reset()
        onDispose { confirmation.reset() }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { confirmation.reset() }

    BackHandler(enabled = enabled) {
        if (confirmation.onBack()) {
            onExit()
        } else {
            onShowHint()
        }
    }
}
