package dev.leonlatsch.photok.other.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityOptionsCompat

fun <I> ActivityResultLauncher<I>.launchAndIgnoreTimer(
    input: I,
    activity: Activity?,
    options: ActivityOptionsCompat? = null,
) {
    launch(input, options)
    activity?.getBaseApplication()?.ignoreNextTimeout()
}

fun Context.startActivityAndIgnoreTimer(intent: Intent, activity: Activity?) {
    startActivity(intent)
    activity?.getBaseApplication()?.ignoreNextTimeout()

}