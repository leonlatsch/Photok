package dev.leonlatsch.photok.other

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

object AndroidFeatures {
    @ChecksSdkIntAtLeast(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun detectScreenRecording(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    }

    @ChecksSdkIntAtLeast(Build.VERSION_CODES.Q)
    fun mediaStore(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }
}