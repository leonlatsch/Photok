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
import android.os.Build
import androidx.core.content.ContextCompat.startForegroundService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.other.sendEmail
import dev.leonlatsch.photok.security.LegacyEncryptionMigrator
import dev.leonlatsch.photok.security.LegacyEncryptionState
import dev.leonlatsch.photok.security.migration.MigrationService
import dev.leonlatsch.photok.settings.data.Config
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class InitialSubStage(val value: Int) {
    INITIAL(0),
    BACKUP(1),
    PERMISSION(2),
    READY(3),
}

sealed interface LegacyEncryptionMigrationUiState {
    data class Initial(
        val stage: InitialSubStage = InitialSubStage.INITIAL,
    ) : LegacyEncryptionMigrationUiState
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

sealed interface LegacyEncryptionMigrationUiEvent {
    data class StartMigration(val context: Context) : LegacyEncryptionMigrationUiEvent
    data class SwitchStage(val stage: InitialSubStage) : LegacyEncryptionMigrationUiEvent
    data class SendErrorReport(
        val context: Context,
        val error: Throwable
    ) : LegacyEncryptionMigrationUiEvent
}

@HiltViewModel
class LegacyEncryptionMigrationViewModel @Inject constructor(
    @ApplicationContext context: Context,
    legacyEncryptionMigrator: LegacyEncryptionMigrator,
    config: Config,
) : ViewModel() {

    private val initialStage = MutableStateFlow(InitialSubStage.INITIAL)

    init {
        if (config.legacyCurrentlyMigrating) {
            handleUiEvent(LegacyEncryptionMigrationUiEvent.StartMigration(context))
        }
    }

    val uiState = combine(
        legacyEncryptionMigrator.state,
        initialStage
    ) { state, stage ->
        when (state) {
            is LegacyEncryptionState.Initial -> LegacyEncryptionMigrationUiState.Initial(stage)
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
        LegacyEncryptionMigrationUiState.Initial()
    )

    fun handleUiEvent(event: LegacyEncryptionMigrationUiEvent) {
        when (event) {
            is LegacyEncryptionMigrationUiEvent.StartMigration -> {
                val serviceIntent = Intent(event.context, MigrationService::class.java)
                startForegroundService(event.context, serviceIntent)
            }
            is LegacyEncryptionMigrationUiEvent.SwitchStage -> {
                initialStage.update { event.stage }
            }

            is LegacyEncryptionMigrationUiEvent.SendErrorReport -> {
                val email = event.context.getString(R.string.settings_other_feedback_mail_emailaddress)
                val subject =
                    "Photok Migration Error Report (App ${BuildConfig.VERSION_NAME} / Android ${Build.VERSION.RELEASE})"

                val text = """
                    Photok error migration report.
                    
                    Please don't change the content below.
                    
                    ${event.error.stackTraceToString()}
                """.trimIndent()

                event.context.sendEmail(
                    email = email,
                    subject = subject,
                    text = text,
                    chooserTitle = "Send Error Report"
                )
            }
        }
    }
}