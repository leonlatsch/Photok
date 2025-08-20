/*
 *   Copyright 2020-2022 Leon Latsch
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

package dev.leonlatsch.photok.security

import android.app.Application
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val SHA_256 = "SHA-256"
private const val AES = "AES"
private const val AES_ALGORITHM = "AES/GCM/NoPadding"

sealed interface LegacyEncryptionState {
    data object Initial : LegacyEncryptionState
    data class Running(
        val processedFiles: Int = 0,
        val totalFiles: Int = 0,
    ) : LegacyEncryptionState

    data class Error(val error: Throwable) : LegacyEncryptionState
    data object Success : LegacyEncryptionState
}


@Singleton
class LegacyEncryptionMigrator @Inject constructor(
    private val encryptedStorageManager: EncryptedStorageManager,
    private val app: Application,
) {

    val state = MutableStateFlow<LegacyEncryptionState>(LegacyEncryptionState.Initial)


    private var key: SecretKeySpec? = null
    private var iv: IvParameterSpec? = null

    private val mutex = Mutex()

    suspend fun init(password: String) = mutex.withLock {
        if (key == null || iv == null) {
            key = genSecKey(password)
            iv = genLegacyIv(password)
        }
    }

    suspend fun migrate() = mutex.withLock {
        if (key == null || iv == null) {

            state.update {
                LegacyEncryptionState.Error(IllegalStateException("Encryption not initialized"))
            }

            return@withLock
        }


        try {
            val allFiles = app.fileList().filter { it.contains("photok") }

            state.update {
                LegacyEncryptionState.Running(
                    processedFiles = 0,
                    totalFiles = allFiles.size,
                )
            }

            var processedFiles = 0
            var error: Throwable? = null

            for (file in allFiles) {
                migrateSingleFile(file)
                    .onFailure {
                        error = it
                        break
                    }

                processedFiles++

                state.update {
                    (it as? LegacyEncryptionState.Running)?.copy(processedFiles = processedFiles)
                        ?: it
                }
            }

            return if (error == null) {
                state.update {
                    LegacyEncryptionState.Success
                }
            } else {
                state.update {
                    LegacyEncryptionState.Error(error)
                }
            }
        } catch (e: Exception) {
            state.update {
                LegacyEncryptionState.Error(e)
            }
        }
    }

    private suspend fun migrateSingleFile(fileName: String): Result<Unit> {
        val migrationFileName = ".migrated~${fileName}"

        try {
            val origInput = app.openFileInput(fileName)
            val legacyInputStream = openLegacyCipherInputStream(origInput)
            val newOutputStream = encryptedStorageManager.internalOpenEncryptedFileOutput(
                migrationFileName
            ) ?: return Result.failure(Exception("New output was null"))


            suspendCoroutine { continuation ->
                legacyInputStream.copyTo(newOutputStream)
                legacyInputStream.close()
                newOutputStream.close()

                continuation.resume(Unit)
            }

            encryptedStorageManager.internalDeleteFile(fileName)
            encryptedStorageManager.renameFile(
                currentFileName = migrationFileName,
                newFileName = fileName,
            )

            return Result.success(Unit)
        } catch (e: Exception) {
            encryptedStorageManager.internalDeleteFile(migrationFileName)
            return Result.failure(e)
        }
    }

    @Throws
    private fun openLegacyCipherInputStream(inputStream: InputStream): CipherInputStream {
        val cipher = Cipher.getInstance(AES_ALGORITHM).apply {
            init(Cipher.DECRYPT_MODE, key, this@LegacyEncryptionMigrator.iv)
        }

        return CipherInputStream(inputStream, cipher)
    }

    private fun genSecKey(password: String): SecretKeySpec {
        val md = MessageDigest.getInstance(SHA_256)
        val bytes = md.digest(password.toByteArray(StandardCharsets.UTF_8))
        return SecretKeySpec(bytes, AES)
    }

    private fun genLegacyIv(password: String): IvParameterSpec {
        val iv = ByteArray(16)
        val charArray = password.toCharArray()
        val firstChars = charArray.take(16)
        for (i in firstChars.indices) {
            iv[i] = firstChars[i].toByte()
        }

        return IvParameterSpec(iv)
    }
}
