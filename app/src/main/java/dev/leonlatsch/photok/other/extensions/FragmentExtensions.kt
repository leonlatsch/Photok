/*
 *   Copyright 2020-2021 Leon Latsch
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

package dev.leonlatsch.photok.other.extensions

import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.lifecycle.withCreationCallback
import kotlinx.coroutines.launch


inline fun Fragment.launchLifecycleAwareJob(
    state: Lifecycle.State = Lifecycle.State.CREATED,
    crossinline block: suspend () -> Unit
) = viewLifecycleOwner.lifecycleScope.launch { repeatOnLifecycle(state) { block() } }


/**
 * Create a view model with assisted injection. This is a workaround for the missing support of assisted injection in Hilt.
 */
inline fun <FactoryType, reified ViewModelType : ViewModel> Fragment.assistedViewModel(
    crossinline viewModelProducer: (FactoryType) -> ViewModelType
) = lazy {
    ViewModelProvider(
        viewModelStore,
        defaultViewModelProviderFactory,
        defaultViewModelCreationExtras.withCreationCallback<FactoryType> { factory ->
            viewModelProducer(factory)
        }
    )[ViewModelType::class.java]
}

fun Fragment.finishOnBackWhileStarted(enabled: Boolean = true) {
    activity?.onBackPressedDispatcher?.addCallback(
        owner = viewLifecycleOwner,
        enabled = enabled,
    ) {
        activity?.finish()
    }
}