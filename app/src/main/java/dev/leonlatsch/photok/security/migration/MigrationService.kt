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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.security.LegacyEncryptionMigrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val CHANNEL_ID = "MigrationChannel"
private const val SERVICE_ID = 1


@AndroidEntryPoint
class MigrationService : Service() {

    @Inject
    lateinit var legacyEncryptionMigrator: LegacyEncryptionMigrator

    private val supervisorJob = Job()

    private val scope = CoroutineScope(supervisorJob + Dispatchers.IO)
    private var notificationManager: NotificationManager? = null



    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(SERVICE_ID, createNotification(0))
        scope.launch {
            legacyEncryptionMigrator.migrate()

            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        }

        scope.launch {
            legacyEncryptionMigrator.progress.collect {
                updateNotification(it)
            }
        }


        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        supervisorJob.cancel()
    }

    private fun updateNotification(progress: Int) {
        val notification = if (progress < 100) {
            createNotification(progress)
        } else {
            createFinishedNotification()
        }
        notificationManager?.notify(SERVICE_ID, notification)
    }

    private fun createNotification(progress: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Migration in Progress")
            .setContentText("Progress: $progress%")
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .build()
    }

    private fun createFinishedNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Migration Done")
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .build()
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Migration Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(serviceChannel)
    }
}