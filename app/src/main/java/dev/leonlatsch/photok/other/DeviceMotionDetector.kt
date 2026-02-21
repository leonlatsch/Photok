/*
 *   Copyright 2020-2026 Leon Latsch
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

package dev.leonlatsch.photok.other

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dev.leonlatsch.photok.ApplicationState
import dev.leonlatsch.photok.BaseApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class DeviceMotionDetector @Inject constructor(
    private val app: Application,
) : DefaultLifecycleObserver, SensorEventListener {

    interface Listener {
        fun onShake()
        fun onFlip(isFaceUp: Boolean)
    }

    private val sensorManager =
        app.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val gravitySensor =
        sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

    private val scope = CoroutineScope(Dispatchers.Default)

    private var listener: Listener? = null

    // Shake detection
    private var lastShakeTime = 0L
    private val shakeThreshold = 2.7f
    private val shakeSlopTimeMs = 500

    // Flip detection
    private var isFaceUp = true
    private val flipThreshold = 7f

    private var isRegistered = false
    private var shouldRegister = false

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    // ---------------- Lifecycle ----------------

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        if (app !is BaseApplication) return

        scope.launch {
            app.state.collectLatest {
                shouldRegister = it == ApplicationState.UNLOCKED

                when (it) {
                    ApplicationState.LOCKED -> unregister()
                    ApplicationState.UNLOCKED -> register()
                }
            }
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        register()
    }

    override fun onPause(owner: LifecycleOwner) {
        unregister()
    }

    private fun register() {
        if (isRegistered) return
        if (!shouldRegister) return

        Timber.d("Registering accelerometer and gravity sensors")

        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        gravitySensor?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        isRegistered = true
    }

    private fun unregister() {
        if (!isRegistered) return

        Timber.d("Unregistering accelerometer and gravity sensors")
        sensorManager.unregisterListener(this)
        isRegistered = false
    }

    // ---------------- Sensor Callback ----------------

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {

            Sensor.TYPE_ACCELEROMETER -> {
                detectShake(
                    event.values[0],
                    event.values[1],
                    event.values[2]
                )
            }

            Sensor.TYPE_GRAVITY -> {
                detectFlip(event.values[2])
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    // ---------------- Detection Logic ----------------

    private fun detectShake(x: Float, y: Float, z: Float) {
        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)

        if (gForce > shakeThreshold) {
            val now = System.currentTimeMillis()

            if (lastShakeTime + shakeSlopTimeMs > now) return

            lastShakeTime = now
            listener?.onShake()
        }
    }

    private fun detectFlip(z: Float) {
        if (z > flipThreshold && !isFaceUp) {
            isFaceUp = true
            listener?.onFlip(true)
        } else if (z < -flipThreshold && isFaceUp) {
            isFaceUp = false
            listener?.onFlip(false)
        }
    }
}