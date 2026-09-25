/*
 *   Copyright 2020–2026 Leon Latsch
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

import dev.leonlatsch.photok.backup.data.BackupMetaData
import dev.leonlatsch.photok.encryption.domain.crypto.CryptoEngine
import dev.leonlatsch.photok.encryption.domain.models.Session
import java.io.InputStream
import javax.inject.Inject

/**
 * Backup Format V5
 *
 *  A ZIP archive with the following structure:
 *
 *  ┌─────────────────────────────────────────┐
 *  │                backup.zip              │
 *  ├─────────────────────────────────────────┤
 *  │ meta.json                              │
 *  │   {                                    │
 *  │     "wrappedVMK": String,              │
 *  │     "params": [VaultProtectionParams], │
 *  │     "photos": [PhotoBackup],           │
 *  │     "albums": [AlbumBackup],           │
 *  │     "albumPhotoRefs":                  │
 *  │        [AlbumPhotoRefBackup],          │
 *  │     "createdAt": Long,                 │
 *  │     "backupVersion": Int               │
 *  │   }                                    │
 *  │                                        │
 *  │ <uuid>.crypt                           │  ← Encrypted photo/video
 *  │ <uuid>.crypt.tn                        │  ← Encrypted thumbnail
 *  │ <uuid>.crypt.vp                        │  ← Encrypted video preview
 *  │ ...                                    │
 *  └─────────────────────────────────────────┘
 *
 * Notes:
 *  - `wrappedVMK` is the wrapped vault master key.
 *  - `params` is the vault protection parameters needed to decrypt the vmk.
 *  - `photos`, `albums`, and `albumPhotoRefs` define the logical structure.
 *  - Each media file is identified by a UUID and encrypted.
 *  - `createdAt` is the timestamp of backup creation.
 *  - `backupVersion` must equal 5 for this format.
 */
class RestoreBackupV5 @Inject constructor(
    private val cryptoEngine: CryptoEngine,
) : RestoreBackupStrategy<BackupMetaData.V5> {

    override fun decrypt(input: InputStream, session: Session): InputStream? =
        cryptoEngine.createDecryptStream(input, session)

    /** Already `.crypt`, the vault uses the name as it stands in the archive. */
    override fun internalFileName(entryName: String): String = entryName
}
