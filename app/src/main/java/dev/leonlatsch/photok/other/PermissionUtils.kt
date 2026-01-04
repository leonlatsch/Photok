


package dev.leonlatsch.photok.other

import android.app.Activity
import androidx.core.app.ActivityCompat

fun Activity.requestInSettings(permission: String): Boolean {
    return ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
}


package dev.leonlatsch.photok.other

import android.app.Activity
import androidx.core.app.ActivityCompat

fun Activity.requestInSettings(permission: String): Boolean {
    return ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
}
