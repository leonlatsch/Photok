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

package dev.leonlatsch.photok.main.ui.navigation

import androidx.compose.animation.core.animate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

val LocalScrollToTopRequests = staticCompositionLocalOf<Flow<Unit>> { emptyFlow() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrollToTopEffect(
    topAppBarState: TopAppBarState,
    scrollToTop: suspend () -> Unit,
) {
    val requests = LocalScrollToTopRequests.current
    val currentScrollToTop by rememberUpdatedState(scrollToTop)

    LaunchedEffect(requests, topAppBarState) {
        requests.collect {
            coroutineScope {
                launch { currentScrollToTop() }
                launch {
                    animate(topAppBarState.heightOffset, 0f) { value, _ ->
                        topAppBarState.heightOffset = value
                    }
                }
            }
        }
    }
}
