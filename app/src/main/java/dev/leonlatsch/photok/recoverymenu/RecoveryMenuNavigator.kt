


package dev.leonlatsch.photok.recoverymenu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import dev.leonlatsch.photok.main.ui.MainActivity
import javax.inject.Inject

class RecoveryMenuNavigator @Inject constructor() {

    fun navigate(navigationEvent: NavigationEvent, activity: AppCompatActivity) {
        when (navigationEvent) {
            NavigationEvent.OpenPhotok -> navigateOpenPhotok(activity)
            NavigationEvent.AfterResetHideApp -> navigateAfterResetHideApp(activity)
        }
    }

    private fun navigateAfterResetHideApp(activity: AppCompatActivity) {
        activity.finish()
    }

    private fun navigateOpenPhotok(activity: AppCompatActivity) {
        val intent = Intent(activity, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        activity.startActivity(intent)
        activity.finish()
    }

    sealed class NavigationEvent {
        object OpenPhotok : NavigationEvent()
        object AfterResetHideApp : NavigationEvent()
    }
}

package dev.leonlatsch.photok.recoverymenu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import dev.leonlatsch.photok.main.ui.MainActivity
import javax.inject.Inject

class RecoveryMenuNavigator @Inject constructor() {

    fun navigate(navigationEvent: NavigationEvent, activity: AppCompatActivity) {
        when (navigationEvent) {
            NavigationEvent.OpenPhotok -> navigateOpenPhotok(activity)
            NavigationEvent.AfterResetHideApp -> navigateAfterResetHideApp(activity)
        }
    }

    private fun navigateAfterResetHideApp(activity: AppCompatActivity) {
        activity.finish()
    }

    private fun navigateOpenPhotok(activity: AppCompatActivity) {
        val intent = Intent(activity, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        activity.startActivity(intent)
        activity.finish()
    }

    sealed class NavigationEvent {
        object OpenPhotok : NavigationEvent()
        object AfterResetHideApp : NavigationEvent()
    }
}