/*
 *   Copyright 2020-2026 Leon Latsch
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
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import dev.leonlatsch.photok.core.R
import timber.log.Timber

fun Context.openUrl(url: String?) {
    url ?: return

    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = url.toUri()

    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Timber.e(e)
    }
}

fun Activity.overrideTransitionSlideInEnter() {
    overridePendingTransition(
        R.anim.slide_in_from_right,
        R.anim.slide_out_to_left,
    )
}

fun Activity.overrideTransitionSlideOutExit() {
    overridePendingTransition(
        R.anim.slide_in_from_left,
        R.anim.slide_out_to_right,
    )
}

fun Activity.overrideTransitionSlideUpEnter() {
    overridePendingTransition(
        R.anim.slide_to_top,
        0,
    )
}

fun Activity.overrideTransitionSlideDownExit() {
    overridePendingTransition(
        0,
        R.anim.slide_to_bottom,
    )
}

/**
 * Whether [permission] has been permanently denied, meaning the system will no longer
 * show its request dialog and the user has to grant it from the app's Settings page instead.
 *
 * Only reliable once the permission has actually been requested at least once in this
 * install - on a cold start before any request, this returns false for every permission.
 */
fun Activity.shouldRequestPermissionInSettings(permission: String): Boolean {
    val hasPermission = ContextCompat.checkSelfPermission(
        this,
        permission,
    ) == PackageManager.PERMISSION_GRANTED

    val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
        this,
        permission,
    )

    return !hasPermission && shouldShowRationale
}
