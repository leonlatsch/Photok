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

package dev.leonlatsch.photok.encryption.integration

import dev.leonlatsch.photok.encryption.domain.LegacyEncryption
import dev.leonlatsch.photok.encryption.domain.crypto.CbcCryptoEngine
import dev.leonlatsch.photok.encryption.domain.crypto.KeyGen
import dev.leonlatsch.photok.encryption.domain.crypto.LegacyGcmCryptoEngine
import dev.leonlatsch.photok.encryption.domain.models.VaultSession
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * Integration test simulating the crypto portion of the 1.x.x → 3.x.x file migration.
 *
 * A file encrypted with the legacy AES/GCM engine (no header) is decrypted and then
 * re-encrypted using the modern AES/CBC engine (V2 header), matching what
 * [dev.leonlatsch.photok.encryption.migration.LegacyEncryptionMigrator] performs.
 */
@RunWith(RobolectricTestRunner::class)
class CryptoMigrationV1ToV3Test {

    private val legacyEngine = LegacyGcmCryptoEngine()
    private val cbcEngine = CbcCryptoEngine()
    private val legacyEncryption = LegacyEncryption()
    private val keyGen = KeyGen()

    @Test
    fun `migrating small file from V1 GCM to V3 CBC preserves plaintext`() {
        val password = "legacy-password-123"
        val plaintext = "This is a 1.x.x encrypted photo file.".toByteArray()

        val decrypted = migrateV1ToV3(password, plaintext)

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun `migrating large file from V1 GCM to V3 CBC preserves plaintext`() {
        val password = "legacy-password-123"
        val plaintext = ByteArray(1_500_000) { (it % 256).toByte() }

        val decrypted = migrateV1ToV3(password, plaintext)

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun `migrated file has V2 header byte`() {
        val password = "legacy-password-123"
        val plaintext = "some content".toByteArray()
        val legacySession = legacyEncryption.obtainSession(password)
        val vmk = keyGen.generateVaultMasterKey()
        val vaultSession = VaultSession(vmk)

        // Encrypt with legacy GCM
        val gcmOutput = ByteArrayOutputStream()
        val gcmStream = legacyEngine.createEncryptStream(gcmOutput, legacySession)!!
        gcmStream.write(plaintext)
        gcmStream.close()

        // Decrypt legacy GCM
        val decryptStream = legacyEngine.createDecryptStream(
            ByteArrayInputStream(gcmOutput.toByteArray()),
            legacySession,
        )!!
        val decryptedBytes = decryptStream.readBytes()

        // Re-encrypt with modern CBC
        val cbcOutput = ByteArrayOutputStream()
        val cbcStream = cbcEngine.createEncryptStream(cbcOutput, vaultSession)!!
        cbcStream.write(decryptedBytes)
        cbcStream.close()

        val migratedBytes = cbcOutput.toByteArray()

        assertEquals("Migrated file must start with version byte 0x02", 0x02.toByte(), migratedBytes[0])
    }

    // --- helpers ---

    private fun migrateV1ToV3(password: String, plaintext: ByteArray): ByteArray {
        val legacySession = legacyEncryption.obtainSession(password)
        val vmk = keyGen.generateVaultMasterKey()
        val vaultSession = VaultSession(vmk)

        // Step 1: Encrypt with legacy GCM (simulates existing 1.x.x file on disk)
        val gcmOutput = ByteArrayOutputStream()
        val gcmEncryptStream = legacyEngine.createEncryptStream(gcmOutput, legacySession)!!
        gcmEncryptStream.write(plaintext)
        gcmEncryptStream.close()

        // Step 2: Decrypt from GCM (what the migrator does when reading the legacy file)
        val gcmDecryptStream = legacyEngine.createDecryptStream(
            ByteArrayInputStream(gcmOutput.toByteArray()),
            legacySession,
        )!!
        val decryptedBytes = gcmDecryptStream.readBytes()

        // Step 3: Re-encrypt with modern CBC (what the migrator writes into the new file)
        val cbcOutput = ByteArrayOutputStream()
        val cbcEncryptStream = cbcEngine.createEncryptStream(cbcOutput, vaultSession)!!
        cbcEncryptStream.write(decryptedBytes)
        cbcEncryptStream.close()

        // Step 4: Decrypt the migrated CBC file to verify it's readable
        val cbcDecryptStream = cbcEngine.createDecryptStream(
            ByteArrayInputStream(cbcOutput.toByteArray()),
            vaultSession,
        )!!
        return cbcDecryptStream.readBytes()
    }

    private fun assertEquals(message: String, expected: Byte, actual: Byte) =
        org.junit.Assert.assertEquals(message, expected, actual)
}
