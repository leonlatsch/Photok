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
import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.encryption.domain.crypto.CryptoEngine
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.inject.Inject

/**
 * Encrypted files in the app's cache dir. Only for data that can be recreated from the vault.
 */
class VaultCacheStorage @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val cryptoEngine: CryptoEngine,
    private val app: Application,
) : EncryptedStorage {
    override fun openEncryptedInput(fileName: String): CipherInputStream? = try {
        val session = requireNotNull(sessionRepository.get())
        val input = FileInputStream(cacheFile(fileName))
        cryptoEngine.createDecryptStream(input, session)
    } catch (e: Exception) {
        Timber.e(e)
        null
    }

    override fun openEncryptedOutput(fileName: String): CipherOutputStream? = try {
        val session = requireNotNull(sessionRepository.get())
        val file = cacheFile(fileName)
        file.parentFile?.mkdirs()
        cryptoEngine.createEncryptStream(FileOutputStream(file), session)
    } catch (e: Exception) {
        Timber.e(e)
        null
    }

    override fun deleteEncryptedFile(fileName: String): Boolean =
        cacheFile(fileName).delete()

    override fun encryptedFileExists(fileName: String): Boolean =
        cacheFile(fileName).exists()

    override fun renameEncryptedFile(currentFileName: String, newFileName: String): Boolean =
        cacheFile(currentFileName).renameTo(cacheFile(newFileName))

    fun listFiles(dir: String): List<File> =
        cacheFile(dir).listFiles()?.toList().orEmpty()

    fun deleteDir(dir: String): Boolean =
        cacheFile(dir).deleteRecursively()

    private fun cacheFile(fileName: String): File = File(app.cacheDir, fileName)
}
