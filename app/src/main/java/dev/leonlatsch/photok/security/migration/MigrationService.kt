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

package dev.leonlatsch.photok.security.migration

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.notifications.NotificationChannels
import dev.leonlatsch.photok.notifications.createAllNotificationChannels
import dev.leonlatsch.photok.security.LegacyEncryptionMigrator
import dev.leonlatsch.photok.security.LegacyEncryptionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val CHANNEL_ID = "BackgroundTasks"
private const val SERVICE_ID = 1001

@AndroidEntryPoint
class MigrationService : Service() {

    @Inject
    lateinit var legacyEncryptionMigrator: LegacyEncryptionMigrator

    private val supervisorJob = Job()

    private val scope = CoroutineScope(supervisorJob + Dispatchers.IO)
    private lateinit var notificationManager: NotificationManagerCompat



    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = NotificationManagerCompat.from(this)
        notificationManager.createAllNotificationChannels(this)
    }

    override fun onTimeout(startId: Int) {
        super.onTimeout(startId)
        supervisorJob.cancel()
    }

    @SuppressLint("InlinedApi")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ServiceCompat.startForeground(this@MigrationService, SERVICE_ID, createInitialNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)

        scope.launch {
            legacyEncryptionMigrator.migrate()

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        scope.launch {
            legacyEncryptionMigrator.state.collectLatest {
                val notification = when (it) {
                    is LegacyEncryptionState.Running -> createNotification(it)
                    is LegacyEncryptionState.Success -> createFinishedNotification()
                    is LegacyEncryptionState.Error -> createErrorNotification(it.error)
                    is LegacyEncryptionState.Initial -> createInitialNotification()
                }

                postNotification(notification)
            }
        }


        return START_STICKY
    }

    private fun postNotification(notification: Notification) {
        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(SERVICE_ID, notification)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        supervisorJob.cancel()
    }

    private fun createInitialNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.migration_running_title))
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setOngoing(true)
            .build()
    }

    private fun createNotification(state: LegacyEncryptionState.Running): Notification {
        val humanReadableProgress = ((state.processedFiles.toFloat() / state.totalFiles.toFloat()) * 100).toInt()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.migration_running_title))
            .setContentText(getString(R.string.migration_running_progress, state.processedFiles, state.totalFiles))
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setProgress(100, humanReadableProgress, false)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    private fun createFinishedNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.migration_done_title))
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setOngoing(false)
            .setAutoCancel(true)
            .build()
    }

    private fun createErrorNotification(error: Throwable): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(error.message ?: resources.getString(R.string.common_error))
            .setSmallIcon(R.drawable.ic_warning)
            .setOngoing(false)
            .setAutoCancel(true)
            .build()
    }
}