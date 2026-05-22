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

package dev.leonlatsch.photok.encryption.crypto

import dev.leonlatsch.photok.encryption.domain.models.EncryptionVersionByte
import org.junit.Assert.assertEquals
import org.junit.Test

class EncryptionVersionByteTest {

    @Test
    fun `fromValue 0x01 returns One`() {
        assertEquals(EncryptionVersionByte.One, EncryptionVersionByte.fromValue(0x01))
    }

    @Test
    fun `fromValue 0x02 returns Two`() {
        assertEquals(EncryptionVersionByte.Two, EncryptionVersionByte.fromValue(0x02))
    }

    @Test(expected = IllegalStateException::class)
    fun `fromValue unknown byte throws IllegalStateException`() {
        EncryptionVersionByte.fromValue(0x00)
    }

    @Test(expected = IllegalStateException::class)
    fun `fromValue 0xFF throws IllegalStateException`() {
        EncryptionVersionByte.fromValue(0xFF.toByte())
    }

    @Test
    fun `headerSize for V1 is 33 bytes`() {
        // 1 (version byte) + 16 (SALT_SIZE) + 16 (IV_SIZE) = 33
        assertEquals(33, EncryptionVersionByte.One.headerSize)
    }

    @Test
    fun `headerSize for V2 is 17 bytes`() {
        // 1 (version byte) + 16 (IV_SIZE) = 17
        assertEquals(17, EncryptionVersionByte.Two.headerSize)
    }

    @Test
    fun `version byte values are correct`() {
        assertEquals(0x01.toByte(), EncryptionVersionByte.One.value)
        assertEquals(0x02.toByte(), EncryptionVersionByte.Two.value)
    }
}
