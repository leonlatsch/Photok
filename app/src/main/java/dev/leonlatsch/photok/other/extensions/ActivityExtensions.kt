package dev.leonlatsch.photok.other.extensions

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.leonlatsch.photok.BaseApplication
import kotlinx.coroutines.launch

/**
 * Get the "application" as [BaseApplication] from any activity.
 */
fun Activity.getBaseApplication(): BaseApplication = application as BaseApplication

/**
 * Compat method to hide the system ui.
 * Uses window insets from api 30 and higher.
 */
fun Activity.hideSystemUI() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.insetsController?.let {
            it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            it.hide(WindowInsets.Type.systemBars())
        }
    } else {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Hide the nav bar and status bar
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        // Keep the app content behind the bars even if user swipes them up
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }
}

/**
 * Compat method to show the system ui.
 * Uses window insets from api 30 and higher.
 */
fun Activity.showSystemUI() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        window.insetsController?.show(WindowInsets.Type.systemBars())
    } else {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }
}
inline fun AppCompatActivity.launchLifecycleAwareJob(
    state: Lifecycle.State = Lifecycle.State.CREATED,
    crossinline block: suspend () -> Unit
) = lifecycleScope.launch { repeatOnLifecycle(state) { block() } }
