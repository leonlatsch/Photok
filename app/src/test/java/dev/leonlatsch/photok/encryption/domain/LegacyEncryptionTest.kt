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

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class LegacyEncryptionTest {

    private val legacyEncryption = LegacyEncryption()

    @Test
    fun `obtainSession key is SHA-256 of password UTF-8 bytes`() {
        val password = "myPassword123"
        val session = legacyEncryption.obtainSession(password)

        val expectedKeyBytes = MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray(StandardCharsets.UTF_8))

        assertArrayEquals(expectedKeyBytes, session.key.encoded)
        assertEquals("AES", session.key.algorithm)
    }

    @Test
    fun `obtainSession IV uses first 16 chars of password as bytes`() {
        val password = "abcdefghijklmnopqrstuvwxyz"
        val session = legacyEncryption.obtainSession(password)

        val expectedIv = password.toCharArray().take(16).map { it.code.toByte() }.toByteArray()

        assertArrayEquals(expectedIv, session.iv.iv)
    }

    @Test
    fun `obtainSession IV is always 16 bytes`() {
        val session = legacyEncryption.obtainSession("any-password")

        assertEquals(16, session.iv.iv.size)
    }

    @Test
    fun `obtainSession short password zero-pads the IV`() {
        val password = "short" // 5 chars, fewer than 16
        val session = legacyEncryption.obtainSession(password)

        val iv = session.iv.iv
        assertEquals(16, iv.size)

        for (i in password.indices) {
            assertEquals("IV byte $i should match password char", password[i].code.toByte(), iv[i])
        }
        for (i in password.length until 16) {
            assertEquals("IV byte $i should be zero-padded", 0.toByte(), iv[i])
        }
    }

    @Test
    fun `obtainSession with exactly 16-char password fills IV completely`() {
        val password = "1234567890abcdef" // exactly 16 chars
        val session = legacyEncryption.obtainSession(password)

        val expectedIv = password.toCharArray().map { it.code.toByte() }.toByteArray()

        assertArrayEquals(expectedIv, session.iv.iv)
    }

    @Test
    fun `obtainSession produces different sessions for different passwords`() {
        val session1 = legacyEncryption.obtainSession("password-one")
        val session2 = legacyEncryption.obtainSession("password-two")

        val keysMatch = session1.key.encoded.contentEquals(session2.key.encoded)
        val ivsMatch = session1.iv.iv.contentEquals(session2.iv.iv)

        assert(!keysMatch) { "Different passwords must produce different keys" }
        assert(!ivsMatch) { "Different passwords must produce different IVs" }
    }
}
