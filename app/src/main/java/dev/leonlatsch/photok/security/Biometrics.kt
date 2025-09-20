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

package dev.leonlatsch.photok.security

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
        .build()

    biometricPrompt.authenticate(promptInfo)
}

