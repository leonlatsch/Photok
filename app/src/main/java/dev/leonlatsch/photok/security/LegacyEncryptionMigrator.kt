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
import dev.leonlatsch.photok.model.database.entity.PHOTOK_FILE_EXTENSION
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import dev.leonlatsch.photok.settings.data.Config
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val MIGRATIED_FILE_PREFIX = ".migrated~"

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
    private val legacyEncryptionManager: LegacyEncryptionManagerImpl,
    private val encryptedStorageManager: EncryptedStorageManager,
    private val app: Application,
    private val config: Config,
) {

    val state = MutableStateFlow<LegacyEncryptionState>(LegacyEncryptionState.Initial)

    private val mutex = Mutex()

    fun migrationNeeded(): Boolean {
        return app.fileList().any { it.contains("photok") }
    }

    suspend fun migrate() = mutex.withLock {
        if (!legacyEncryptionManager.isReady) {
            state.update {
                LegacyEncryptionState.Error(IllegalStateException("Encryption not initialized"))
            }

            return@withLock
        }

        config.legacyCurrentlyMigrating = true

        try {
            val legacyFiles = app.fileList()
                .filter { it.contains("photok") && !it.startsWith(MIGRATIED_FILE_PREFIX) }

            state.update {
                LegacyEncryptionState.Running(
                    processedFiles = 0,
                    totalFiles = legacyFiles.size,
                )
            }

            var processedFiles = 0
            var error: Throwable? = null

            for (file in legacyFiles) {
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
                postMigrate()

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

    private suspend fun postMigrate() = suspendCoroutine { continuation ->
        val migratedFile = app.fileList().filter { it.contains(MIGRATIED_FILE_PREFIX) }

        for (file in migratedFile) {
            val targetFileName = file
                .removePrefix(MIGRATIED_FILE_PREFIX)
                .replace("photok", PHOTOK_FILE_EXTENSION)

            encryptedStorageManager.renameFile(file, targetFileName)
        }

        config.legacyCurrentlyMigrating = false
        continuation.resume(Unit)
    }

    private suspend fun migrateSingleFile(fileName: String): Result<Unit> {
        val migratedFile = "$MIGRATIED_FILE_PREFIX${fileName}"

        try {
            encryptedStorageManager.internalDeleteFile(migratedFile)

            val origInput = app.openFileInput(fileName)
            val legacyInputStream = legacyEncryptionManager.createCipherInputStream(
                origInput,
                null
            ) ?: return Result.failure(Exception("Old output was null"))
            val newOutputStream = encryptedStorageManager.internalOpenEncryptedFileOutput(
                migratedFile
            ) ?: return Result.failure(Exception("New output was null"))


            suspendCoroutine { continuation ->
                legacyInputStream.copyTo(newOutputStream)
                legacyInputStream.close()
                newOutputStream.close()

                continuation.resume(Unit)
            }

            encryptedStorageManager.internalDeleteFile(fileName)

            return Result.success(Unit)
        } catch (e: Exception) {
            encryptedStorageManager.internalDeleteFile(migratedFile)
            return Result.failure(e)
        }
    }
}
