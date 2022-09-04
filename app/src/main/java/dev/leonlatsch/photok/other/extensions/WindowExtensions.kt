/*
 *   Copyright 2020-2022 Leon Latsch
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

import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsets
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

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

/**
 * Set the color of the windows status bar.
 */
fun Window.setStatusBarColorRes(@ColorRes colorRes: Int) {
    statusBarColor = ContextCompat.getColor(context, colorRes)
}

/**
 * Set the color of the windows navigation bar.
 */
fun Window.setNavBarColorRes(@ColorRes colorRes: Int) {
    navigationBarColor = ContextCompat.getColor(context, colorRes)
}