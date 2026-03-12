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

package dev.leonlatsch.photok.telemetry.domain

import android.content.Context
import com.telemetrydeck.sdk.TelemetryDeck
import com.telemetrydeck.sdk.providers.DefaultParameterProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.settings.data.Config
import javax.inject.Inject
import javax.inject.Singleton

private const val TelemetryDeckAppId = "C2F73045-3C8D-4912-951B-FE9894262E86" // Not a secret. Fine to have in code

@Singleton
class TelemetryService @Inject constructor(
    private val config: Config,
    @ApplicationContext private val context: Context,
) {
    fun setup() {
        if (config.telemetryEnabled) {
            val builder = TelemetryDeck.Builder()
                .appID(TelemetryDeckAppId)
                .addProvider(
                    DefaultParameterProvider(
                        mapOf("flavor" to BuildConfig.FLAVOR)
                    )
                )
                .showDebugLogs(BuildConfig.DEBUG)

            TelemetryDeck.start(context, builder)
        } else {
            TelemetryDeck.stop()
        }
    }
}