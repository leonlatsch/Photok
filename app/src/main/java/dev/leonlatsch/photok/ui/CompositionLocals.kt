package dev.leonlatsch.photok.ui

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.fragment.app.Fragment

val LocalFragment: ProvidableCompositionLocal<Fragment?> = compositionLocalOf { null }