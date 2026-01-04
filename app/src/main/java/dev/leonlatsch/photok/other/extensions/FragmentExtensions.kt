


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