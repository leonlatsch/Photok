package dev.leonlatsch.photok.forwarddialer.usecase

import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext

import javax.inject.Inject

class IsAirplaneModeOnUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {

    operator fun invoke(): Boolean {
        return Settings.System.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON,
            0
        ) != 0
    }
}