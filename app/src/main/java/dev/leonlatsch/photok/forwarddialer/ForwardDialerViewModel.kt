


package dev.leonlatsch.photok.forwarddialer

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.forwarddialer.usecase.IsAirplaneModeOnUseCase
import dev.leonlatsch.photok.other.SingleLiveEvent
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.settings.data.Config.Companion.TIMESTAMP_LAST_RECOVERY_START_DEFAULT
import javax.inject.Inject

const val RECOVERY_MENU_MILLIS_THRESHOLD = 5000L

@HiltViewModel
class ForwardDialerViewModel @Inject constructor(
    private val isAirplaneModeOn: IsAirplaneModeOnUseCase,
    private val config: Config
) : ViewModel() {

    val navigationEvent = SingleLiveEvent<ForwardDialerNavigator.NavigationEvent>()

    fun evaluateNavigation() = if (isAirplaneModeOn()) {
        val now = System.currentTimeMillis()
        val lastRecoveryStart = config.timestampLastRecoveryStart

        val millisSinceLastRecoveryStart = now - lastRecoveryStart

        if (millisSinceLastRecoveryStart < RECOVERY_MENU_MILLIS_THRESHOLD) {
            navigationEvent.value = ForwardDialerNavigator.NavigationEvent.OpenRecoveryMenu
            config.timestampLastRecoveryStart = TIMESTAMP_LAST_RECOVERY_START_DEFAULT
        } else {
            config.timestampLastRecoveryStart = now
            navigateToDialer()
        }

    } else {
        navigateToDialer()
    }

    private fun navigateToDialer() {
        navigationEvent.value = ForwardDialerNavigator.NavigationEvent.ForwardToDialer
    }
}

package dev.leonlatsch.photok.forwarddialer

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.forwarddialer.usecase.IsAirplaneModeOnUseCase
import dev.leonlatsch.photok.other.SingleLiveEvent
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.settings.data.Config.Companion.TIMESTAMP_LAST_RECOVERY_START_DEFAULT
import javax.inject.Inject

const val RECOVERY_MENU_MILLIS_THRESHOLD = 5000L

@HiltViewModel
class ForwardDialerViewModel @Inject constructor(
    private val isAirplaneModeOn: IsAirplaneModeOnUseCase,
    private val config: Config
) : ViewModel() {

    val navigationEvent = SingleLiveEvent<ForwardDialerNavigator.NavigationEvent>()

    fun evaluateNavigation() = if (isAirplaneModeOn()) {
        val now = System.currentTimeMillis()
        val lastRecoveryStart = config.timestampLastRecoveryStart

        val millisSinceLastRecoveryStart = now - lastRecoveryStart

        if (millisSinceLastRecoveryStart < RECOVERY_MENU_MILLIS_THRESHOLD) {
            navigationEvent.value = ForwardDialerNavigator.NavigationEvent.OpenRecoveryMenu
            config.timestampLastRecoveryStart = TIMESTAMP_LAST_RECOVERY_START_DEFAULT
        } else {
            config.timestampLastRecoveryStart = now
            navigateToDialer()
        }

    } else {
        navigateToDialer()
    }

    private fun navigateToDialer() {
        navigationEvent.value = ForwardDialerNavigator.NavigationEvent.ForwardToDialer
    }
}