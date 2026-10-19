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

package dev.leonlatsch.photok.encryption.domain

import androidx.lifecycle.LiveData
import com.nulabinc.zxcvbn.Zxcvbn
import dev.leonlatsch.photok.encryption.domain.PasswordUtils.PASSWORD_MIN_LENGTH
import dev.leonlatsch.photok.encryption.domain.models.PasswordStrength
import java.util.Locale

/**
 * Utils to validate passwords and estimate their strength.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
object PasswordUtils {

    /**
     * Validates a password string using [PASSWORD_MIN_LENGTH].
     */
    fun validatePassword(password: String) = password.isNotEmpty()
            && password.length >= PASSWORD_MIN_LENGTH

    /**
     * Indicates if two passwords are equal.
     */
    fun passwordsNotEmptyAndEqual(password: String, confirmPassword: String) = password.isNotEmpty()
            && confirmPassword.isNotEmpty()
            && password == confirmPassword

    fun passwordsNotEmptyAndEqual(password: LiveData<String>, confirmPassword: LiveData<String>) =
        passwordsNotEmptyAndEqual(password.value!!, confirmPassword.value!!)

    /**
     * Indicates if two passwords are valid and equal.
     */
    fun validatePasswords(password: String, confirmPassword: String) = validatePassword(password)
            && validatePassword(confirmPassword)
            && passwordsNotEmptyAndEqual(password, confirmPassword)

    fun validatePasswords(password: LiveData<String>, confirmPassword: LiveData<String>) =
        validatePasswords(password.value!!, confirmPassword.value!!)

    private val zxcvbn by lazy { Zxcvbn() }

    const val CRACK_TIME_GUESSES_PER_SECOND = 100_000

    /**
     * Estimates the strength of [password] with zxcvbn. Blocking and possibly slow, call it off the main thread.
     * [locale] is used for the feedback messages.
     */
    fun estimateStrength(password: String, locale: Locale): PasswordStrength {
        val strength = zxcvbn.measure(password)
        val result = PasswordStrength(
            score = strength.score,
            crackTimeSeconds = strength.guesses / CRACK_TIME_GUESSES_PER_SECOND,
            warning = strength.feedback.getWarning(locale)?.takeIf { it.isNotBlank() },
            suggestions = strength.feedback.getSuggestions(locale),
        )
        strength.wipe()
        return result
    }

    private const val PASSWORD_MIN_LENGTH = 6
}
