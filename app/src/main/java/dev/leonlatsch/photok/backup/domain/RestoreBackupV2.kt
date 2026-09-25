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
import dev.leonlatsch.photok.encryption.domain.crypto.LegacyGcmCryptoEngine
import dev.leonlatsch.photok.encryption.domain.models.Session
import dev.leonlatsch.photok.model.database.entity.LEGACY_PHOTOK_FILE_EXTENSION
import dev.leonlatsch.photok.model.database.entity.PHOTOK_FILE_EXTENSION
import java.io.InputStream
import javax.inject.Inject

/**
 * Backup Format V2
 *
 *  A ZIP archive with the following structure:
 *
 *  ┌───────────────────────────────┐
 *  │           backup.zip          │
 *  ├───────────────────────────────┤
 *  │ meta.json                     │
 *  │   {                           │
 *  │     "password": String,       │
 *  │     "photos": [PhotoBackup],  │
 *  │     "createdAt": Long,        │
 *  │     "backupVersion": Int      │
 *  │   }                           │
 *  │                               │
 *  │ <uuid>.photok                 │  ← Encrypted photo/video
 *  │ <uuid>.photok.tn              │  ← Encrypted thumbnail
 *  │ <uuid>.photok.vp              │  ← Encrypted video preview
 *  │ ...                           │
 *  └───────────────────────────────┘
 *
 * Notes:
 *  - `password` is used to check before decryption.
 *  - Only `photos` are tracked (no album or albumPhotoRefs yet).
 *  - Each media file is identified by a UUID and encrypted.
 *  - File extension matches V3: `.photok.*`.
 *  - `backupVersion` must equal 2 for this format.
 */
class RestoreBackupV2 @Inject constructor(
    private val legacyGcmCryptoEngine: LegacyGcmCryptoEngine,
) : RestoreBackupStrategy<BackupMetaData.V2> {

    override fun decrypt(input: InputStream, session: Session): InputStream? =
        legacyGcmCryptoEngine.createDecryptStream(input, session)

    /** The vault stores `.crypt`, this format still carried the old `.photok` name. */
    override fun internalFileName(entryName: String): String = entryName.replace(
        oldValue = LEGACY_PHOTOK_FILE_EXTENSION,
        newValue = PHOTOK_FILE_EXTENSION,
    )
}
