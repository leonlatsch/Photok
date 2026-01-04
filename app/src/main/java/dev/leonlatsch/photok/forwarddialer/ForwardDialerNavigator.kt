


package dev.leonlatsch.photok.forwarddialer

import android.content.Intent
import dev.leonlatsch.photok.recoverymenu.RecoveryMenuActivity
import timber.log.Timber
import javax.inject.Inject

class ForwardDialerNavigator @Inject constructor() {

    fun navigate(navigationEvent: NavigationEvent, activity: ForwardDialerActivity) {
        when (navigationEvent) {
            NavigationEvent.ForwardToDialer -> navigateForwardToDialer(activity)
            NavigationEvent.OpenRecoveryMenu -> navigateOpenRecoveryMenu(activity)
        }
    }

    private fun navigateOpenRecoveryMenu(activity: ForwardDialerActivity) {
        Timber.d("opening recovery menu")
        val intent = Intent(activity, RecoveryMenuActivity::class.java)
        activity.apply {
            startActivity(intent)
            finish()
        }
    }

    private fun navigateForwardToDialer(activity: ForwardDialerActivity) {
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        activity.apply {
            startActivity(dialIntent)
            finishAndRemoveTask()
        }
    }

    sealed class NavigationEvent {
        object OpenRecoveryMenu : NavigationEvent()
        object ForwardToDialer : NavigationEvent()
    }
}

package dev.leonlatsch.photok.forwarddialer

import android.content.Intent
import dev.leonlatsch.photok.recoverymenu.RecoveryMenuActivity
import timber.log.Timber
import javax.inject.Inject

class ForwardDialerNavigator @Inject constructor() {

    fun navigate(navigationEvent: NavigationEvent, activity: ForwardDialerActivity) {
        when (navigationEvent) {
            NavigationEvent.ForwardToDialer -> navigateForwardToDialer(activity)
            NavigationEvent.OpenRecoveryMenu -> navigateOpenRecoveryMenu(activity)
        }
    }

    private fun navigateOpenRecoveryMenu(activity: ForwardDialerActivity) {
        Timber.d("opening recovery menu")
        val intent = Intent(activity, RecoveryMenuActivity::class.java)
        activity.apply {
            startActivity(intent)
            finish()
        }
    }

    private fun navigateForwardToDialer(activity: ForwardDialerActivity) {
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        activity.apply {
            startActivity(dialIntent)
            finishAndRemoveTask()
        }
    }

    sealed class NavigationEvent {
        object OpenRecoveryMenu : NavigationEvent()
        object ForwardToDialer : NavigationEvent()
    }
}