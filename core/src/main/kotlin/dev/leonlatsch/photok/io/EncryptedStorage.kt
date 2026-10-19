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

package dev.leonlatsch.photok.io

import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream

/**
 * Files encrypted with the vault master key of the current session.
 *
 * File names are relative to the root of the storage.
 */
interface EncryptedStorage {
    fun openEncryptedInput(fileName: String): CipherInputStream?

    fun openEncryptedOutput(fileName: String): CipherOutputStream?

    fun deleteEncryptedFile(fileName: String): Boolean

    fun encryptedFileExists(fileName: String): Boolean

    fun renameEncryptedFile(currentFileName: String, newFileName: String): Boolean
}
