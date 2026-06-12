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
import dev.leonlatsch.photok.backup.data.BackupMetaData
import dev.leonlatsch.photok.encryption.domain.LegacyEncryption
import dev.leonlatsch.photok.encryption.domain.crypto.KeyGen
import dev.leonlatsch.photok.encryption.domain.crypto.SALT_SIZE
import dev.leonlatsch.photok.encryption.domain.handlers.VaultProtectionHandler
import dev.leonlatsch.photok.encryption.domain.models.CreateRequest
import dev.leonlatsch.photok.encryption.domain.models.EncryptionVersionByte
import dev.leonlatsch.photok.encryption.domain.models.Kdf
import dev.leonlatsch.photok.encryption.domain.models.LegacySession
import dev.leonlatsch.photok.encryption.domain.models.Session
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import dev.leonlatsch.photok.encryption.domain.models.VaultProtection
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionType
import dev.leonlatsch.photok.encryption.domain.models.VaultSession
import dev.leonlatsch.photok.io.IO
import dev.leonlatsch.photok.model.database.entity.PHOTOK_FILE_EXTENSION
import org.mindrot.jbcrypt.BCrypt
import javax.inject.Inject
import kotlin.io.encoding.Base64

class UnlockBackupUseCase @Inject constructor(
    private val legacyEncryption: LegacyEncryption,
    private val io: IO,
    private val keyGen: KeyGen,
    private val passwordVaultProtectionHandler: VaultProtectionHandler<UnlockRequest.Password, CreateRequest.Password>,
) {
    suspend operator fun invoke(
        uri: Uri,
        metaData: BackupMetaData,
        password: String,
    ): Result<Session> = runCatching {
        when (metaData) {
            is BackupMetaData.V1 -> createSessionFromV1toV3(metaData.password, password)
            is BackupMetaData.V2 -> createSessionFromV1toV3(metaData.password, password)
            is BackupMetaData.V3 -> createSessionFromV1toV3(metaData.password, password)
            is BackupMetaData.V4 -> createSessionFromV4(uri, metaData.password, password)
            is BackupMetaData.V5 -> createSessionFromV5(password, metaData)
        }
    }

    private fun createSessionFromV1toV3(pwHash: String, password: String): LegacySession {
        require(BCrypt.checkpw(password, pwHash))

        return legacyEncryption.obtainSession(password)
    }

    private fun createSessionFromV4(uri: Uri, pwHash: String, password: String): Session {
        require(BCrypt.checkpw(password, pwHash))

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

    private suspend fun createSessionFromV5(
        password: String,
        metaData: BackupMetaData.V5
    ): Session {
        val wrappedVmk = Base64.decode(metaData.wrappedVMK)

        val vmk = passwordVaultProtectionHandler.unlock(
            UnlockRequest.Password(password),
            VaultProtection(
                id = "",
                type = VaultProtectionType.Password,
                wrappedVMK = wrappedVmk,
                params = metaData.params,
            )
        )

        return VaultSession(
            vmk = vmk,
        )
    }
}