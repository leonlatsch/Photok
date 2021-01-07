/*
 *   Copyright 2020-2021 Leon Latsch
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

package dev.leonlatsch.photok

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import dev.leonlatsch.photok.other.restartAppLifecycle
import dev.leonlatsch.photok.other.setAppDesign
import dev.leonlatsch.photok.settings.Config
import timber.log.Timber
import javax.inject.Inject

/**
 * Base Application class.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltAndroidApp
class BaseApplication : Application(), LifecycleObserver {

    @Inject
    lateinit var config: Config

    val applicationState = ObservableField<ApplicationState>()

    private var wentToBackgroundAt = 0L

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        setAppDesign(config.systemDesign)
    }

    /**
     * Call [restartAppLifecycle] when app was ON_STOP for at least the configured time.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForeground() {
        if (ignoreNextTimeout) {
            ignoreNextTimeout = false
            return
        }

        if (config.securityLockTimeout != -1
            && wentToBackgroundAt != 0L
            && System.currentTimeMillis() - wentToBackgroundAt >= config.securityLockTimeout
        ) {
            restartAppLifecycle(this)
        }
    }

    /**
     * Saves the ON_STOP timestamp
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackground() {
        wentToBackgroundAt = System.currentTimeMillis()
    }

    companion object {
        private var ignoreNextTimeout = false

        /**
         * Ignore next check for lock timeout.
         */
        fun ignoreNextTimeout() {
            ignoreNextTimeout = true
        }
    }
}