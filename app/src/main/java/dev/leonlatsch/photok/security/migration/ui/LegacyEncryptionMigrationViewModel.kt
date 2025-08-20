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

package dev.leonlatsch.photok.security.migration.ui

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startForegroundService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.security.LegacyEncryptionMigrator
import dev.leonlatsch.photok.security.LegacyEncryptionState
import dev.leonlatsch.photok.security.migration.MigrationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface LegacyEncryptionMigrationUiState {
    data object Initial : LegacyEncryptionMigrationUiState
    data class Migrating(
        val totalFiles: Int = 0,
        val processedFiles: Int = 0,
    ) : LegacyEncryptionMigrationUiState {
        val progressPercentage: Float = processedFiles.toFloat() / totalFiles.toFloat()
    }

    data object Success : LegacyEncryptionMigrationUiState
    data class Error(
        val error: Throwable,
    ) : LegacyEncryptionMigrationUiState
}

@HiltViewModel
class LegacyEncryptionMigrationViewModel @Inject constructor(
    private val legacyEncryptionMigrator: LegacyEncryptionMigrator
) : ViewModel() {

    val uiState = legacyEncryptionMigrator.state.map { state ->
        when (state) {
            is LegacyEncryptionState.Initial -> LegacyEncryptionMigrationUiState.Initial
            is LegacyEncryptionState.Error -> LegacyEncryptionMigrationUiState.Error(state.error)
            is LegacyEncryptionState.Running -> LegacyEncryptionMigrationUiState.Migrating(
                totalFiles = state.totalFiles,
                processedFiles = state.processedFiles,
            )
            is LegacyEncryptionState.Success -> LegacyEncryptionMigrationUiState.Success
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        LegacyEncryptionMigrationUiState.Initial
    )

    fun startMigration(context: Context) {
        // TODO: Check if on progress
        val serviceIntent = Intent(context, MigrationService::class.java)
        startForegroundService(context, serviceIntent)
    }
}