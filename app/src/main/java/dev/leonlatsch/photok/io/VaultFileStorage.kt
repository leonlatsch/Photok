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

import android.app.Application
import android.content.Context
import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.encryption.domain.crypto.CryptoEngine
import okio.IOException
import timber.log.Timber
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.inject.Inject

class VaultFileStorage @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val cryptoEngine: CryptoEngine,
    private val app: Application,
) {
    fun openEncryptedInput(filename: String): CipherInputStream? = try {
        val session = sessionRepository.require()
        val input = app.openFileInput(filename)
        cryptoEngine.createDecryptStream(input, session)
    } catch (e: IOException) {
        Timber.e(e)
        null
    }

    fun openEncryptedOutput(fileName: String): CipherOutputStream? = try {
        val session = sessionRepository.require()
        val output = app.openFileOutput(fileName, Context.MODE_PRIVATE)
        cryptoEngine.createEncryptStream(output, session)
    } catch (e: IOException) {
        Timber.e(e)
        null
    }

    fun deleteEncryptedFile(fileName: String): Boolean {
        val success = app.deleteFile(fileName)
        if (!success) {
            Timber.e("Error deleting internal file: $fileName")
        }

        return success
    }

    fun renameEncryptedFile(currentFileName: String, newFileName: String): Boolean {
        val currentFile = app.getFileStreamPath(currentFileName)
        val newFile = app.getFileStreamPath(newFileName)
        return currentFile.renameTo(newFile)
    }
}