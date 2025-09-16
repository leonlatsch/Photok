/*
 *   Copyright 2020-2024 Leon Latsch
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

package dev.leonlatsch.photok.security.migration

import android.app.Application
import dev.leonlatsch.photok.databinding.BindingConverters
import dev.leonlatsch.photok.model.database.entity.LEGACY_PHOTOK_FILE_EXTENSION
import dev.leonlatsch.photok.model.database.entity.PHOTOK_FILE_EXTENSION
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import dev.leonlatsch.photok.model.io.IO
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.settings.data.Config
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.security.GeneralSecurityException
import java.security.SecureRandom
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
    @LegacyEncryptionManager private val legacyEncryptionManager: EncryptionManager,
    private val encryptedStorageManager: EncryptedStorageManager,
    private val app: Application,
    private val config: Config,
    private val io: IO,
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
            val legacyFiles = app.fileList().filter {
                it.contains(LEGACY_PHOTOK_FILE_EXTENSION) && !it.startsWith(
                    MIGRATIED_FILE_PREFIX
                )
            }

            if (legacyFiles.isEmpty()) {
                state.update { LegacyEncryptionState.Success }
                postMigrate()
                return@withLock
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

            encryptedStorageManager.internalRenameFile(file, targetFileName)
        }

        config.legacyCurrentlyMigrating = false
    }

    private suspend fun migrateSingleFile(legacyName: String): Result<Unit> = runCatching {
        require(legacyName.contains(LEGACY_PHOTOK_FILE_EXTENSION)) { "Not legacy file" }

        val tmpName = "$MIGRATIED_FILE_PREFIX$legacyName"
        val finalName = legacyName.replace(
            oldValue = LEGACY_PHOTOK_FILE_EXTENSION,
            newValue = PHOTOK_FILE_EXTENSION,
        )

        // Clean any stale temp from prior crashes
        encryptedStorageManager.internalDeleteFile(tmpName)

        if (app.getFileStreamPath(legacyName).length() == 0L) {
            encryptedStorageManager.internalDeleteFile(legacyName)
            Timber.d("Empty legacy file: $legacyName - Deleting and continuing")
            return@runCatching
        }

        val originalInput = app.openFileInput(legacyName)
        val encryptedLegacyInput = legacyEncryptionManager.createCipherInputStream(originalInput)
        val encryptedOutput = encryptedStorageManager.internalOpenEncryptedFileOutput(tmpName)

        requireNotNull(encryptedLegacyInput) { "Legacy input was null" }
        requireNotNull(encryptedOutput) { "New output was null" }


        io.copy(
            input = encryptedLegacyInput,
            output = encryptedOutput,
        ).onFailure {
            throw Exception( buildError(it, legacyName, tmpName), it )
        }

        encryptedLegacyInput.close()
        encryptedOutput.flush()
        encryptedOutput.close()

        // Finalize atomically: temp -> final (non-overwriting)
        encryptedStorageManager.internalDeleteFile(finalName)
        encryptedStorageManager.internalRenameFile(tmpName, finalName)

        // Only now delete original
        encryptedStorageManager.internalDeleteFile(legacyName)
    }

    private fun buildError(error: Throwable, legacyFileName: String, tmpName: String): String {
        val legacyFile = app.openFileInput(legacyFileName)
        val size = BindingConverters.formatByteSizeConverter(legacyFile.available().toLong())

        val filesList = app.fileList()
        val numLegacyFiles = filesList.count { it.contains(LEGACY_PHOTOK_FILE_EXTENSION) }
        val numFinishedFiles = filesList.count { it.contains(PHOTOK_FILE_EXTENSION) }
        val numTempFiles = filesList.count { it.contains(MIGRATIED_FILE_PREFIX) }

        return """
            Migration Error.
             
            File Size: $size
            File Name: $legacyFileName
            Temp Name: $tmpName
            
            Legacy Files: $numLegacyFiles
            Finished Files: $numFinishedFiles
            Temp Files: $numTempFiles
            
            Causing Exception: ${error.message}
        """.trimIndent()
    }
}
