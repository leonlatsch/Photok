/*
 *   Copyright 2020–2026 Leon Latsch
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

package dev.leonlatsch.photok.main.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionType
import dev.leonlatsch.photok.news.newfeatures.ui.FEATURE_VERSION_CODE
import dev.leonlatsch.photok.news.newfeatures.ui.NewFeaturesSheet
import dev.leonlatsch.photok.recovery.ui.RecoveryPhraseSheet
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.settings.ui.compose.LocalConfig
import dev.leonlatsch.photok.telemetry.domain.TelemetryEnabledByDefault
import dev.leonlatsch.photok.telemetry.ui.TelemetryOptInQuestionSheet
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private enum class StartupSheet {
    RecoveryPhrase,
    NewFeatures,
    TelemetryOptIn,
}

@Composable
fun StartupSheets() {
    val config = LocalConfig.current ?: return
    var currentSheet by rememberSaveable { mutableStateOf<StartupSheet?>(null) }

    LaunchedEffect(currentSheet) {
        if (currentSheet != null) return@LaunchedEffect

        delay(300.milliseconds)
        currentSheet = nextStartupSheet(config)
    }

    when (currentSheet) {
        StartupSheet.RecoveryPhrase -> RecoveryPhraseSheet(
            onDismissRequest = {
                config.systemShowRecoveryPhraseSheet = false
                currentSheet = null
            },
        )

        StartupSheet.NewFeatures -> NewFeaturesSheet(
            onDismissRequest = {
                config.systemLastFeatureVersionCode = FEATURE_VERSION_CODE
                currentSheet = null
            },
        )

        StartupSheet.TelemetryOptIn -> TelemetryOptInQuestionSheet(
            onDismissRequest = {
                config.telemetryAskedForOptIn = true
                currentSheet = null
            },
        )

        null -> Unit
    }
}

private fun nextStartupSheet(config: Config): StartupSheet? {
    if (config.systemShowRecoveryPhraseSheet) {
        return StartupSheet.RecoveryPhrase
    }

    if (config.systemLastFeatureVersionCode == 0) {
        config.systemLastFeatureVersionCode = FEATURE_VERSION_CODE
    }
    if (config.systemLastFeatureVersionCode < FEATURE_VERSION_CODE) {
        return StartupSheet.NewFeatures
    }

    if (
        !TelemetryEnabledByDefault
        && !config.telemetryAskedForOptIn
        && !config.justFinishedSetup
        && config.lastUsedUnlockMethod != VaultProtectionType.RecoveryPhrase
    ) {
        return StartupSheet.TelemetryOptIn
    }

    return null
}
