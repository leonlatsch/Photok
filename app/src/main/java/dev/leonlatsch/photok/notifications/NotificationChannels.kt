package dev.leonlatsch.photok.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.annotation.StringRes
import androidx.core.app.NotificationManagerCompat
import dev.leonlatsch.photok.R

enum class NotificationChannels(val id: String, @StringRes val displayName: Int) {
    BACKGROUND_TASKS("BackgroundTasks", R.string.notification_channel_background_tasks)
}

fun NotificationManagerCompat.createAllNotificationChannels(context: Context) {
    NotificationChannels.entries.forEach { channel ->
        val notificationChannel = NotificationChannel(
            channel.id,
            context.getString(channel.displayName),
            NotificationManager.IMPORTANCE_LOW
        )
         createNotificationChannel(notificationChannel)
    }
}
