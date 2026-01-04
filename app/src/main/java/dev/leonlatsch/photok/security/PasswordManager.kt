


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
    suspend fun storePassword(password: String) {
        val hashedPw = BCrypt.hashpw(password, BCrypt.gensalt())
        config.securityPassword = hashedPw
        config.userSalt = null
        biometricUnlock.reset()
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
    suspend fun storePassword(password: String) {
        val hashedPw = BCrypt.hashpw(password, BCrypt.gensalt())
        config.securityPassword = hashedPw
        config.userSalt = null
        biometricUnlock.reset()
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