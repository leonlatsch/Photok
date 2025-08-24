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
import dev.leonlatsch.photok.model.database.entity.LEGACY_PHOTOK_FILE_EXTENSION
import dev.leonlatsch.photok.model.database.entity.PHOTOK_FILE_EXTENSION
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import dev.leonlatsch.photok.settings.data.Config
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

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
        return app.fileList().any { it.contains(LEGACY_PHOTOK_FILE_EXTENSION) }
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
                .filter {
                    it.contains(LEGACY_PHOTOK_FILE_EXTENSION) && !it.startsWith(
                        MIGRATIED_FILE_PREFIX
                    )
                }

            state.update {
                LegacyEncryptionState.Running(
                    processedFiles = 0,
                    totalFiles = legacyFiles.size,
                )
            }

            var processedFiles = 0
            var error: Throwable? = null

            for (legacyFile in legacyFiles) {
                migrateSingleFile(legacyFile)
                    .onFailure {
                        error = it
                        break
                    }

                processedFiles++

                state.update {
                    require(it is LegacyEncryptionState.Running)
                    it.copy(processedFiles = processedFiles)
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

    private fun postMigrate() {
        val migratedFile = app.fileList().filter { it.contains(MIGRATIED_FILE_PREFIX) }

        for (file in migratedFile) {
            val targetFileName = file
                .removePrefix(MIGRATIED_FILE_PREFIX)
                .replace(LEGACY_PHOTOK_FILE_EXTENSION, PHOTOK_FILE_EXTENSION)

            encryptedStorageManager.renameFile(file, targetFileName)
        }

        config.legacyCurrentlyMigrating = false
    }

    private fun migrateSingleFile(legacyName: String): Result<Unit> = runCatching {
        require(legacyName.contains(LEGACY_PHOTOK_FILE_EXTENSION)) { "Not legacy file" }

        val tmpName = "$MIGRATIED_FILE_PREFIX$legacyName"
        val finalName = legacyName.replace(
            oldValue = LEGACY_PHOTOK_FILE_EXTENSION,
            newValue = PHOTOK_FILE_EXTENSION,
        )

        // Clean any stale temp from prior crashes
        encryptedStorageManager.internalDeleteFile(tmpName)

        app.openFileInput(legacyName).use { origInput ->
            val legacyIn = legacyEncryptionManager.createCipherInputStream(
                input = origInput,
                password = null,
            ) ?: error("Legacy cipher stream null")

            encryptedStorageManager.internalOpenEncryptedFileOutput(tmpName).use { newOut ->
                requireNotNull(newOut) { "New output was null" }
                legacyIn.use { it.copyTo(newOut) }
                newOut.flush()
            }
        }

        // Finalize atomically: temp -> final (non-overwriting)
        require(!encryptedStorageManager.internalFileExists(finalName)) { "Target exists: $finalName" }
        encryptedStorageManager.renameFile(tmpName, finalName)

        // Only now delete original
        encryptedStorageManager.internalDeleteFile(legacyName)
    }
}
