package dev.leonlatsch.photok.di

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Workaround class for injecting into broadcast receiver.
 * This should be remove, once hilt fixes this.
 * Ensures super call to [onReceive].
 *
 * More information: https://github.com/google/dagger/issues/1918#issuecomment-644057247
 *
 * @since 1.2.0
 * @author Leon Latsch
 */
abstract class DaggerBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {}
}