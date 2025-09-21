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

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import javax.crypto.Cipher
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UserCanceledBiometricsException : Exception()

/**
 * Unlocks a [Cipher] that is protected by biometric authentication.
 *
 * Shows a [BiometricPrompt] on a [Fragment] and returns the unlocked cipher
 * if authentication succeeds, or an error if it fails or is canceled.
 *
 * Main tasks:
 * - Display biometric prompt with title, subtitle, and cancel option
 * - Return unlocked cipher or error result
 */
class UnlockCipherUseCase @Inject constructor() {
    suspend operator fun invoke(
        fragment: Fragment,
        cipher: Cipher,
        title: String,
        subtitle: String,
        negativeButtonText: String,
    ): Result<Cipher> = suspendCoroutine { continuation ->
        val biometricPrompt = BiometricPrompt(
            fragment,
            ContextCompat.getMainExecutor(fragment.requireContext()),
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val cipher = result.cryptoObject?.cipher

                    if (cipher != null) {
                        continuation.resume(Result.success(cipher))
                    } else {
                        continuation.resume(Result.failure(IllegalStateException("Cipher is null")))
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    val noErrorMessages = setOf(
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_USER_CANCELED,
                    )

                    val error = if (noErrorMessages.contains(errorCode)) {
                        UserCanceledBiometricsException()
                    } else {
                        Exception(errString.toString())
                    }

                    continuation.resume(Result.failure(error))
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setConfirmationRequired(true)
            .build()

        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }
}