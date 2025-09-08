/*
 *   Copyright 2020-2024 Leon Latsch
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
