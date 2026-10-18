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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class PasswordUtilsTest {

    @Test
    fun `rejects passwords shorter than six characters`() {
        assertFalse(PasswordUtils.validatePassword("abc12"))
        assertTrue(PasswordUtils.validatePassword("abc123"))
    }

    @Test
    fun `requires both passwords to be valid and equal`() {
        assertTrue(PasswordUtils.validatePasswords("abc123", "abc123"))
        assertFalse(PasswordUtils.validatePasswords("abc123", "abc124"))
        assertFalse(PasswordUtils.validatePasswords("abc", "abc"))
    }

    @Test
    fun `scores a common password lower than a long passphrase`() {
        val weak = PasswordUtils.estimateStrength("password", Locale.ENGLISH)
        val strong = PasswordUtils.estimateStrength("violet tractor sings under 47 lamps", Locale.ENGLISH)

        assertEquals(0, weak.score)
        assertEquals(4, strong.score)
        assertTrue(strong.crackTimeSeconds > weak.crackTimeSeconds)
    }

    @Test
    fun `gives feedback for weak passwords`() {
        val strength = PasswordUtils.estimateStrength("password", Locale.ENGLISH)

        assertTrue(strength.warning != null)
        assertTrue(strength.suggestions.isNotEmpty())
    }

    @Test
    fun `uses the bundled feedback translations`() {
        val english = PasswordUtils.estimateStrength("password", Locale.ENGLISH).suggestions

        for (language in listOf("ar", "id", "in", "ru", "sv", "tr", "ur", "zh")) {
            val translated = PasswordUtils.estimateStrength("password", Locale.forLanguageTag(language))

            assertNotEquals(language, english, translated.suggestions)
        }
    }
}
