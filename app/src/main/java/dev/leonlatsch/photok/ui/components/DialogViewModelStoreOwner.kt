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

package dev.leonlatsch.photok.ui.components

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

@Composable
fun DialogViewModelStoreOwner(
    content: @Composable () -> Unit
) {
    val activity = LocalActivity.current as ComponentActivity

    val viewModelStore = remember { ViewModelStore() }

    DisposableEffect(Unit) {
        onDispose {
            viewModelStore.clear()
        }
    }

    val owner = remember(activity, viewModelStore) {
        object : ViewModelStoreOwner,
            HasDefaultViewModelProviderFactory {

            override val viewModelStore: ViewModelStore = viewModelStore

            override val defaultViewModelProviderFactory: ViewModelProvider.Factory =
                activity.defaultViewModelProviderFactory

            override val defaultViewModelCreationExtras: CreationExtras =
                activity.defaultViewModelCreationExtras
        }
    }

    CompositionLocalProvider(
        LocalViewModelStoreOwner provides owner,
        content = content
    )
}