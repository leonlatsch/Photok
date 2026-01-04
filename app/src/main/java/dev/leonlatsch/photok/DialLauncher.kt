


package dev.leonlatsch.photok

import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.di.DaggerBroadcastReceiver
import dev.leonlatsch.photok.main.ui.MainActivity
import dev.leonlatsch.photok.settings.data.Config
import javax.inject.Inject

/**
 * Broadcast receiver for receiving android secret codes.
 *
 * @since 1.2.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class DialLauncher : DaggerBroadcastReceiver() {

    @Inject
    lateinit var config: Config

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        context ?: return
        if (intent?.data?.host == config.securityDialLaunchCode) {
            val launchIntent = Intent(context, MainActivity::class.java)
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(launchIntent)
        }
    }
}

package dev.leonlatsch.photok

import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.di.DaggerBroadcastReceiver
import dev.leonlatsch.photok.main.ui.MainActivity
import dev.leonlatsch.photok.settings.data.Config
import javax.inject.Inject

/**
 * Broadcast receiver for receiving android secret codes.
 *
 * @since 1.2.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class DialLauncher : DaggerBroadcastReceiver() {

    @Inject
    lateinit var config: Config

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        context ?: return
        if (intent?.data?.host == config.securityDialLaunchCode) {
            val launchIntent = Intent(context, MainActivity::class.java)
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(launchIntent)
        }
    }
}