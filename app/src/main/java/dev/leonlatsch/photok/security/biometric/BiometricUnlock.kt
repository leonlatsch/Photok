


package dev.leonlatsch.photok.security.biometric

import androidx.fragment.app.Fragment

interface BiometricUnlock {
    fun areBiometricsAvailable(): Boolean
    fun isSetupAndValid(): Boolean
    suspend fun setup(fragment: Fragment): Result<Unit>
    suspend fun unlock(fragment: Fragment): Result<Unit>
    suspend fun reset(): Result<Unit>
}

package dev.leonlatsch.photok.security.biometric

import androidx.fragment.app.Fragment

interface BiometricUnlock {
    fun areBiometricsAvailable(): Boolean
    fun isSetupAndValid(): Boolean
    suspend fun setup(fragment: Fragment): Result<Unit>
    suspend fun unlock(fragment: Fragment): Result<Unit>
    suspend fun reset(): Result<Unit>
}