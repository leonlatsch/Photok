package dev.leonlatsch.photok.other.extensions

import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.leonlatsch.photok.BaseApplication
import kotlinx.coroutines.launch

/**
 * Get the "application" as [BaseApplication] from any activity.
 */
fun Activity.getBaseApplication(): BaseApplication? = application as? BaseApplication

/**
 * Compat method to hide the system ui.
 * Uses window insets from api 30 and higher.
 */

inline fun AppCompatActivity.launchLifecycleAwareJob(
    state: Lifecycle.State = Lifecycle.State.CREATED,
    crossinline block: suspend () -> Unit
) = lifecycleScope.launch { repeatOnLifecycle(state) { block() } }

fun Activity.hideKeyboard() {
    val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
}
