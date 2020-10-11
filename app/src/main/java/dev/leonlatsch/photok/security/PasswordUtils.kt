/*
 *   Copyright 2020 Leon Latsch
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

import dev.leonlatsch.photok.other.PASSWORD_REGEX
import java.util.regex.Pattern

/**
 * Utils to validate passwords.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
object PasswordUtils {

    /**
     * Validates a password string using [PASSWORD_REGEX].
     */
    fun validatePassword(password: String) = password.isNotEmpty()
            && Pattern.matches(
        PASSWORD_REGEX, password
    )

    /**
     * Indicates if two password equal.
     */
    fun passwordsEqual(password: String, confirmPassword: String) = password == confirmPassword

    /**
     * Indicates if two password are valid and equal.
     */
    fun validatePasswords(password: String, confirmPassword: String) = validatePassword(password)
            && validatePassword(confirmPassword)
            && passwordsEqual(password, confirmPassword)
}