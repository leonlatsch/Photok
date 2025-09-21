/*
 *   Copyright 2020-2024 Leon Latsch
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

package dev.leonlatsch.photok.security.biometric

import android.content.res.Resources
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.security.EncryptionManager
import timber.log.Timber
import javax.crypto.Cipher
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class BiometricUnlockImpl @Inject constructor(
    private val resources: Resources,
    private val encryptionManager: EncryptionManager,
    private val biometricKeyStore: BiometricKeyStore,
) : BiometricUnlock {

    override suspend fun setup(fragment: Fragment): Result<Unit> {
        val currentUserKey = encryptionManager.getKeyOrNull()
        if (currentUserKey == null) {
            return Result.failure(IllegalStateException("EncryptionManager not ready"))
        }


        val result = fragment.biometricAuthentication(
            title = resources.getString(R.string.biometric_unlock_setup_title),
            subtitle = resources.getString(R.string.biometric_unlock_setup_subtitle),
            negativeButtonText = resources.getString(R.string.common_cancel),
        )

        result.onFailure {
            Timber.d("Biometric setup failed: $it")
            return Result.failure(it)
        }

        return biometricKeyStore.storeUserKey(currentUserKey).onFailure {
            Timber.d("Storing user key failed: $it")
        }
    }

    override suspend fun unlock(fragment: Fragment): Result<Unit> {
        val result = fragment.biometricAuthentication(
            title = resources.getString(R.string.biometric_unlock_title),
            subtitle = resources.getString(R.string.biometric_unlock_subtitle),
            negativeButtonText = resources.getString(R.string.biometric_unlock_cancel),
        )

        result.onFailure {
            Timber.w("Biometric unlock failed: $it")
            return Result.failure(it)
        }

        val userKey = biometricKeyStore.getUserKey().onFailure {
            Timber.w("Getting user key failed: $it")
        }.getOrNull()

        userKey ?: return Result.failure(IllegalStateException("Could not load user key"))

        return encryptionManager.initialize(userKey).onFailure {
            Timber.d("EncryptionManager initialization failed: $it")
        }
    }

    override suspend fun reset(): Result<Unit> = runCatching {
        biometricKeyStore.removeStoredUserKey()
    }
}

suspend fun Fragment.biometricAuthentication(
    title: String,
    subtitle: String,
    negativeButtonText: String,
): Result<BiometricPrompt.AuthenticationResult> = suspendCoroutine { continuation ->
    val biometricPrompt = BiometricPrompt(
        this,
        ContextCompat.getMainExecutor(requireContext()),
        object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                continuation.resume(Result.success(result))
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                continuation.resume(Result.failure(Exception(errString.toString())))
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                continuation.resume(Result.failure(Exception("Authentication failed")))
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setNegativeButtonText(negativeButtonText)
        .setConfirmationRequired(true)
        .build()

    val cipher: Cipher = TODO()

    biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
}
