/*
 *   Copyright 2020-2022 Leon Latsch
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

import dev.leonlatsch.photok.other.extensions.empty
import dev.leonlatsch.photok.security.biometric.BiometricUnlock
import dev.leonlatsch.photok.settings.data.Config
import org.mindrot.jbcrypt.BCrypt
import javax.inject.Inject

/**
 * Handles operations that belong to changing or checking the saved password.
 *
 * @author Leon Latsch
 * @since 1.3.1
 */
class PasswordManager @Inject constructor(
    private val config: Config,
    private val biometricUnlock: BiometricUnlock,
) {

    /**
     * Hashes and stores a new password
     */
    fun storePassword(password: String) {
        val hashedPw = BCrypt.hashpw(password, BCrypt.gensalt())
        config.securityPassword = hashedPw
        config.userSalt = null
    }

    /**
     * Checks a plain text password against the stored one
     */
    fun checkPassword(password: String): Boolean =
        checkPassword(password, config.securityPassword)

    /**
     * Checks a plain test against a bcrypt hash
     */
    fun checkPassword(password: String, hash: String?): Boolean =
        BCrypt.checkpw(password, hash)

    /**
     * Set stored password to empty string
     */
    suspend fun resetPassword() {
        config.securityPassword = String.empty
        biometricUnlock.reset()
    }
}