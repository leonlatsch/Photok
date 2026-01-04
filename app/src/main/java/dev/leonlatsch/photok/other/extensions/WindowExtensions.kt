


package dev.leonlatsch.photok.other.extensions

import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager

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
 * Get the device screen size. Uses appropriate API for different android versions.
 */
fun WindowManager.getCompatScreenSize(): Pair<Int, Int> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val bounds = this.currentWindowMetrics.bounds
        Pair(bounds.width(), bounds.height())
    } else {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        this.defaultDisplay.getMetrics(metrics)
        Pair(metrics.widthPixels, metrics.heightPixels)
    }


package dev.leonlatsch.photok.other.extensions

import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager

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
 * Get the device screen size. Uses appropriate API for different android versions.
 */
fun WindowManager.getCompatScreenSize(): Pair<Int, Int> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val bounds = this.currentWindowMetrics.bounds
        Pair(bounds.width(), bounds.height())
    } else {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        this.defaultDisplay.getMetrics(metrics)
        Pair(metrics.widthPixels, metrics.heightPixels)
    }
