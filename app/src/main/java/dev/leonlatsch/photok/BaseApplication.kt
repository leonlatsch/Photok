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
import android.content.Intent
import androidx.lifecycle.*
import dagger.hilt.android.HiltAndroidApp
import dev.leonlatsch.photok.other.setAppDesign
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.settings.Config
import dev.leonlatsch.photok.ui.MainActivity
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

    @Inject
    lateinit var encryptionManager: EncryptionManager

    private var wentToBackgroundAt = 0L

    val rawApplicationState = MutableLiveData(ApplicationState.LOCKED)

    private var ignoreNextTimeout = false

    var applicationState: ApplicationState
        get() = rawApplicationState.value!!
        set(value) = rawApplicationState.postValue(value)

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        setAppDesign(config.systemDesign)
    }

    /**
     * Call [lockApp] when app was ON_STOP for at least the configured time.
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
            lockApp()
        }
    }

    /**
     * Saves the ON_STOP timestamp
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackground() {
        wentToBackgroundAt = System.currentTimeMillis()
    }

    /**
     * Ignore next check for lock timeout.
     */
    fun ignoreNextTimeout() {
        ignoreNextTimeout = true
    }

    /**
     * Reset the [EncryptionManager], set [applicationState] to [ApplicationState.LOCKED] and start [MainActivity] with NEW_TESK.
     */
    fun lockApp() {
        encryptionManager.reset()
        applicationState = ApplicationState.LOCKED
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}