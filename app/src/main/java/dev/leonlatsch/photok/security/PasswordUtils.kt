


package dev.leonlatsch.photok.security

import androidx.lifecycle.LiveData
import dev.leonlatsch.photok.security.PasswordUtils.PASSWORD_MIN_LENGTH

/**
 * Utils to validate passwords.
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

    fun validatePassword(password: LiveData<String>) = validatePassword(password.value!!)

    /**
     * Indicates if two password equal.
     */
    fun passwordsNotEmptyAndEqual(password: String, confirmPassword: String) = password.isNotEmpty()
            && confirmPassword.isNotEmpty()
            && password == confirmPassword

    fun passwordsNotEmptyAndEqual(password: LiveData<String>, confirmPassword: LiveData<String>) =
        passwordsNotEmptyAndEqual(password.value!!, confirmPassword.value!!)

    /**
     * Indicates if two password are valid and equal.
     */
    fun validatePasswords(password: String, confirmPassword: String) = validatePassword(password)
            && validatePassword(confirmPassword)
            && passwordsNotEmptyAndEqual(password, confirmPassword)

    fun validatePasswords(password: LiveData<String>, confirmPassword: LiveData<String>) =
        validatePasswords(password.value!!, confirmPassword.value!!)

    private const val PASSWORD_MIN_LENGTH = 6
}

package dev.leonlatsch.photok.security

import androidx.lifecycle.LiveData
import dev.leonlatsch.photok.security.PasswordUtils.PASSWORD_MIN_LENGTH

/**
 * Utils to validate passwords.
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

    fun validatePassword(password: LiveData<String>) = validatePassword(password.value!!)

    /**
     * Indicates if two password equal.
     */
    fun passwordsNotEmptyAndEqual(password: String, confirmPassword: String) = password.isNotEmpty()
            && confirmPassword.isNotEmpty()
            && password == confirmPassword

    fun passwordsNotEmptyAndEqual(password: LiveData<String>, confirmPassword: LiveData<String>) =
        passwordsNotEmptyAndEqual(password.value!!, confirmPassword.value!!)

    /**
     * Indicates if two password are valid and equal.
     */
    fun validatePasswords(password: String, confirmPassword: String) = validatePassword(password)
            && validatePassword(confirmPassword)
            && passwordsNotEmptyAndEqual(password, confirmPassword)

    fun validatePasswords(password: LiveData<String>, confirmPassword: LiveData<String>) =
        validatePasswords(password.value!!, confirmPassword.value!!)

    private const val PASSWORD_MIN_LENGTH = 6
}