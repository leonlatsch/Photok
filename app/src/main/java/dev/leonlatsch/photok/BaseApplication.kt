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
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import dev.leonlatsch.photok.main.ui.MainActivity
import dev.leonlatsch.photok.model.repositories.CleanupDeadFilesUseCase
import dev.leonlatsch.photok.other.setAppDesign
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.settings.data.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject

/**
 * Base Application class.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltAndroidApp
class BaseApplication : Application(), DefaultLifecycleObserver {

    @Inject
    lateinit var appScope: CoroutineScope

    @Inject
    lateinit var config: Config

    @Inject
    lateinit var encryptionManager: EncryptionManager
    @Inject
    lateinit var cleanupDeadFilesUseCase: CleanupDeadFilesUseCase

    val state = MutableStateFlow(ApplicationState.LOCKED)


    private var wentToBackgroundAt = 0L
    private var ignoreNextTimeout = false

    override fun onCreate() {
        super<Application>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        setAppDesign(config.systemDesign)
        cleanupDeadFilesUseCase()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        appScope.cancel()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

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

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)

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
        state.update { ApplicationState.LOCKED }
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}