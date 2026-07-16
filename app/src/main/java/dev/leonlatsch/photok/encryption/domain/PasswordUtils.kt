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
import dev.leonlatsch.photok.encryption.domain.PasswordUtils.PASSWORD_MIN_LENGTH
import dev.leonlatsch.photok.encryption.domain.models.PasswordStrength
import kotlin.math.log2

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

    /**
     * Estimates the strength of [password] as a [PasswordStrength], using an entropy heuristic
     * that rewards length and character variety but penalizes repeated or sequential patterns.
     * Does not check against dictionaries or known-breached passwords.
     */
    fun calculateStrength(password: String): PasswordStrength {
        if (password.length < PASSWORD_MIN_LENGTH) return PasswordStrength.VERY_WEAK

        val rawEntropyBits = password.length * log2(characterPoolSize(password).toDouble())
        val entropyBits = rawEntropyBits * patternPenaltyFactor(password)

        return when {
            entropyBits >= VERY_STRONG_ENTROPY_THRESHOLD_BITS -> PasswordStrength.VERY_STRONG
            entropyBits >= STRONG_ENTROPY_THRESHOLD_BITS -> PasswordStrength.STRONG
            entropyBits >= MODERATE_ENTROPY_THRESHOLD_BITS -> PasswordStrength.MODERATE
            entropyBits >= WEAK_ENTROPY_THRESHOLD_BITS -> PasswordStrength.WEAK
            else -> PasswordStrength.VERY_WEAK
        }
    }

    /**
     * Size of the character pool [password] draws from, based on which categories are present.
     */
    private fun characterPoolSize(password: String): Int {
        var poolSize = 0
        if (password.any { it.isLowerCase() }) poolSize += LOWERCASE_POOL_SIZE
        if (password.any { it.isUpperCase() }) poolSize += UPPERCASE_POOL_SIZE
        if (password.any { it.isDigit() }) poolSize += DIGIT_POOL_SIZE
        if (password.any { !it.isLetterOrDigit() }) poolSize += SYMBOL_POOL_SIZE
        return poolSize.coerceAtLeast(1)
    }

    /**
     * Factor in `(0, 1]` that discounts entropy for runs of repeated/sequential characters or
     * adjacent keyboard keys (min length [MIN_PATTERN_RUN_LENGTH]). `1.0` if [password] has none.
     */
    private fun patternPenaltyFactor(password: String): Double {
        val patternedCharCount = (sequentialPatternedCharCount(password) + keyboardPatternedCharCount(password))
            .coerceAtMost(password.length)
        val patternedRatio = patternedCharCount.toDouble() / password.length
        return 1.0 - (patternedRatio * PATTERN_PENALTY_WEIGHT)
    }

    /**
     * Counts characters that are part of a repeated or ascending/descending character-code run,
     * e.g. "aaaa" or "1234".
     */
    private fun sequentialPatternedCharCount(password: String): Int {
        var patternedCharCount = 0
        var i = 0

        while (i < password.length - 1) {
            val step = password[i + 1].code - password[i].code
            if (step == -1 || step == 0 || step == 1) {
                var runLength = 2
                while (i + runLength < password.length
                    && password[i + runLength].code - password[i + runLength - 1].code == step
                ) {
                    runLength++
                }
                if (runLength >= MIN_PATTERN_RUN_LENGTH) {
                    patternedCharCount += runLength
                }
                i += runLength
            } else {
                i++
            }
        }

        return patternedCharCount
    }

    /**
     * Counts characters that are part of a run along a keyboard row, e.g. "qwerty" or "asdfgh".
     */
    private fun keyboardPatternedCharCount(password: String): Int {
        var patternedCharCount = 0
        var i = 0
        val lower = password.lowercase()

        while (i < lower.length - 1) {
            val step = keyboardStep(lower[i], lower[i + 1])
            if (step != null) {
                var runLength = 2
                while (i + runLength < lower.length
                    && keyboardStep(lower[i + runLength - 1], lower[i + runLength]) == step
                ) {
                    runLength++
                }
                if (runLength >= MIN_PATTERN_RUN_LENGTH) {
                    patternedCharCount += runLength
                }
                i += runLength
            } else {
                i++
            }
        }

        return patternedCharCount
    }

    /**
     * Column difference between [a] and [b] if both are letters/digits on the same keyboard row
     * and adjacent to each other, `null` otherwise.
     */
    private fun keyboardStep(a: Char, b: Char): Int? {
        val posA = KEYBOARD_POSITIONS[a] ?: return null
        val posB = KEYBOARD_POSITIONS[b] ?: return null
        if (posA.first != posB.first) return null
        val step = posB.second - posA.second
        return step.takeIf { it == 1 || it == -1 }
    }

    private val KEYBOARD_ROWS = listOf(
        "1234567890",
        "qwertyuiop",
        "asdfghjkl",
        "zxcvbnm"
    )

    private val KEYBOARD_POSITIONS: Map<Char, Pair<Int, Int>> = KEYBOARD_ROWS
        .flatMapIndexed { row, chars -> chars.mapIndexed { col, char -> char to (row to col) } }
        .toMap()

    private const val PASSWORD_MIN_LENGTH = 6

    private const val LOWERCASE_POOL_SIZE = 26
    private const val UPPERCASE_POOL_SIZE = 26
    private const val DIGIT_POOL_SIZE = 10
    private const val SYMBOL_POOL_SIZE = 33

    private const val MIN_PATTERN_RUN_LENGTH = 3
    private const val PATTERN_PENALTY_WEIGHT = 0.75

    private const val WEAK_ENTROPY_THRESHOLD_BITS = 20.0
    private const val MODERATE_ENTROPY_THRESHOLD_BITS = 35.0
    private const val STRONG_ENTROPY_THRESHOLD_BITS = 55.0
    private const val VERY_STRONG_ENTROPY_THRESHOLD_BITS = 75.0
}
