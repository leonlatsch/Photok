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

package dev.leonlatsch.photok.other

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.fragment.app.Fragment
import dev.leonlatsch.photok.BaseApplication

/**
 * Sets the visibility to [View.VISIBLE]
 */
fun View.show() {
    this.visibility = View.VISIBLE
}

/**
 * Sets the visibility to [View.GONE]
 */
fun View.hide() {
    this.visibility = View.GONE
}

/**
 * Sets the visibility to [View.INVISIBLE]
 */
fun View.vanish() {
    this.visibility = View.INVISIBLE
}

/**
 * Returns an empty string.
 */
val String.Companion.empty: String
    get() = ""

/**
 * Get the "application" as [BaseApplication] from any activity.
 */
fun Activity.getBaseApplication(): BaseApplication = application as BaseApplication

/**
 * Extension for starting an activity for result and disable lock timer in [BaseApplication].
 */
fun Fragment.startActivityForResultAndIgnoreTimer(intent: Intent, reqCode: Int) {
    startActivityForResult(intent, reqCode)
    BaseApplication.ignoreNextTimeout()
}

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

/**
 * Compat method for adding a [visibilityListener] to the system ui.
 * Uses window insets from api 30 and higher.
 */
fun Window.addSystemUIVisibilityListener(visibilityListener: (Boolean) -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        decorView.setOnApplyWindowInsetsListener { v, insets ->
            val suppliedInsets = v.onApplyWindowInsets(insets)
            visibilityListener(suppliedInsets.isVisible(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()))
            suppliedInsets
        }
    } else {
        @Suppress("DEPRECATION")
        decorView.setOnSystemUiVisibilityChangeListener {
            visibilityListener((it and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0)
        }
    }
}