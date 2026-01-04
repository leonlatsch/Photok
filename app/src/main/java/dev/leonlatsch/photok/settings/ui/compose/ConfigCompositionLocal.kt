package dev.leonlatsch.photok.settings.ui.compose

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import dev.leonlatsch.photok.settings.data.Config

val LocalConfig: ProvidableCompositionLocal<Config?> =
    compositionLocalOf { null }