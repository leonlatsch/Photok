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
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.uicomponnets.Dialogs

private const val EXIT_CONFIRMATION_TIMEOUT_MS = 2_000L

@Composable
fun ExitConfirmationBackHandler(enabled: Boolean) {
    val activity = LocalActivity.current
    val context = LocalContext.current
    val exitHint = stringResource(R.string.common_press_back_again_to_exit)
    var lastBackPress by remember { mutableStateOf<Long?>(null) }

    BackHandler(enabled = enabled) {
        val now = SystemClock.elapsedRealtime()
        val previous = lastBackPress
        if (previous != null && now - previous < EXIT_CONFIRMATION_TIMEOUT_MS) {
            activity?.finish()
        } else {
            lastBackPress = now
            Dialogs.showShortToast(context, exitHint)
        }
    }
}
