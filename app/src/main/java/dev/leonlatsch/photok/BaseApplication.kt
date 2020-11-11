/*
 *   Copyright 2020 Leon Latsch
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import dev.leonlatsch.photok.other.restartAppLifecycle
import dev.leonlatsch.photok.settings.Config
import dev.leonlatsch.photok.ui.StartActivity
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

    private var wentToBackgroundAt = 0L

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    /**
     * Launch [StartActivity] when app was ON_STOP for at least 5 Minutes
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForeground() {
        if (wentToBackgroundAt != 0L && System.currentTimeMillis() - wentToBackgroundAt >= config.securityLockTimeout) { // 5 Minutes
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
}