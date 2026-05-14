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

package dev.leonlatsch.photok.backup.domain

import android.net.Uri
import dev.leonlatsch.photok.encryption.domain.LegacyEncryption
import dev.leonlatsch.photok.encryption.domain.crypto.KeyGen
import dev.leonlatsch.photok.encryption.domain.crypto.SALT_SIZE
import dev.leonlatsch.photok.encryption.domain.models.EncryptionVersionByte
import dev.leonlatsch.photok.encryption.domain.models.Kdf
import dev.leonlatsch.photok.encryption.domain.models.Session
import dev.leonlatsch.photok.encryption.domain.models.VaultSession
import dev.leonlatsch.photok.io.IO
import dev.leonlatsch.photok.model.database.entity.PHOTOK_FILE_EXTENSION
import javax.inject.Inject

class UnlockBackupUseCase @Inject constructor(
    private val legacyEncryption: LegacyEncryption,
    private val io: IO,
    private val keyGen: KeyGen,
) {
    operator fun invoke(uri: Uri, version: Int, password: String): Result<Session> = when (version) {
        1, 2, 3 -> runCatching {
            legacyEncryption.obtainSession(password)
        }

        4 -> runCatching { createSessionFromV4(uri, password) }
        else -> Result.failure(IllegalStateException())
    }

    private fun createSessionFromV4(uri: Uri, password: String): Session {
        val zipInputStream = io.zip.openZipInput(uri)

        var ze = zipInputStream.nextEntry

        while (ze != null) {
            if (!ze.name.endsWith(PHOTOK_FILE_EXTENSION)) {
                ze = zipInputStream.nextEntry
                continue
            }


            val version = zipInputStream.read().toByte().let { EncryptionVersionByte.fromValue(it) }
            require(version == EncryptionVersionByte.One)

            val salt = ByteArray(SALT_SIZE)
            zipInputStream.read(salt, 0, salt.size)

            val v1Key = keyGen.derivePasswordKeyEncryptionKey(
                password,
                salt,
                Kdf.PBKDF2WithHmacSHA256,
                100_000,
                256,
            )

            return VaultSession(v1Key)
        }

        error("No file found in backup. Cannot derive salt.")
    }
}