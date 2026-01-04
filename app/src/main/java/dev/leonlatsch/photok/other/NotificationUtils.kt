


package dev.leonlatsch.photok.other

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import dev.leonlatsch.photok.other.extensions.startActivityAndIgnoreTimer

fun Context.areNotificationsEnabled(): Boolean {
    return NotificationManagerCompat.from(this).areNotificationsEnabled()
}

fun Activity.openNotificationSettings() {
    val intent = Intent().apply {
        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        putExtra(Settings.EXTRA_APP_PACKAGE, this@openNotificationSettings.packageName)
    }
    startActivityAndIgnoreTimer(intent, this)
}


package dev.leonlatsch.photok.other

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import dev.leonlatsch.photok.other.extensions.startActivityAndIgnoreTimer

fun Context.areNotificationsEnabled(): Boolean {
    return NotificationManagerCompat.from(this).areNotificationsEnabled()
}

fun Activity.openNotificationSettings() {
    val intent = Intent().apply {
        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        putExtra(Settings.EXTRA_APP_PACKAGE, this@openNotificationSettings.packageName)
    }
    startActivityAndIgnoreTimer(intent, this)
}
