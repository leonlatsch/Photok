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

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordUtilsTest {

    @Test
    fun `validatePassword returns false for empty string`() {
        assertFalse(PasswordUtils.validatePassword(""))
    }

    @Test
    fun `validatePassword returns false for fewer than 6 characters`() {
        assertFalse(PasswordUtils.validatePassword("abc"))
        assertFalse(PasswordUtils.validatePassword("12345"))
    }

    @Test
    fun `validatePassword returns true for exactly 6 characters`() {
        assertTrue(PasswordUtils.validatePassword("abcdef"))
    }

    @Test
    fun `validatePassword returns true for more than 6 characters`() {
        assertTrue(PasswordUtils.validatePassword("a1b2c3d4e5f6"))
    }

    @Test
    fun `passwordsNotEmptyAndEqual returns true for matching non-empty passwords`() {
        assertTrue(PasswordUtils.passwordsNotEmptyAndEqual("password123", "password123"))
    }

    @Test
    fun `passwordsNotEmptyAndEqual returns false for different passwords`() {
        assertFalse(PasswordUtils.passwordsNotEmptyAndEqual("password1", "password2"))
    }

    @Test
    fun `passwordsNotEmptyAndEqual returns false when first password is empty`() {
        assertFalse(PasswordUtils.passwordsNotEmptyAndEqual("", "password"))
    }

    @Test
    fun `passwordsNotEmptyAndEqual returns false when second password is empty`() {
        assertFalse(PasswordUtils.passwordsNotEmptyAndEqual("password", ""))
    }

    @Test
    fun `passwordsNotEmptyAndEqual returns false when both are empty`() {
        assertFalse(PasswordUtils.passwordsNotEmptyAndEqual("", ""))
    }

    @Test
    fun `validatePasswords returns true for valid matching passwords`() {
        assertTrue(PasswordUtils.validatePasswords("abcdef", "abcdef"))
    }

    @Test
    fun `validatePasswords returns false when passwords are valid but do not match`() {
        assertFalse(PasswordUtils.validatePasswords("abcdef", "xyzxyz"))
    }

    @Test
    fun `validatePasswords returns false when passwords are too short`() {
        assertFalse(PasswordUtils.validatePasswords("abc", "abc"))
    }

    @Test
    fun `validatePasswords returns false when one password is too short`() {
        assertFalse(PasswordUtils.validatePasswords("abcdef", "abc"))
    }

    @Test
    fun `validatePasswords returns false when both passwords are empty`() {
        assertFalse(PasswordUtils.validatePasswords("", ""))
    }
}
