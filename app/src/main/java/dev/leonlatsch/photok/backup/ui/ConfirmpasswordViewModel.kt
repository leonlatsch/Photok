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

package dev.leonlatsch.photok.backup.ui

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.VaultService
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConfirmPasswordUiState(
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
) {
    data class Inputs(
        val password: String = "",
        val loading: Boolean = false,
        val error: String? = null,
        val success: Boolean = false,
    )
}

sealed interface ConfirmPasswordUiEvent {
    data class PasswordValueChange(val value: String) : ConfirmPasswordUiEvent
    data object ConfirmPassword : ConfirmPasswordUiEvent
}

@HiltViewModel
class ConfirmpasswordViewModel @Inject constructor(
    private val vaultService: VaultService,
    private val resources: Resources,
): ViewModel() {

    private val inputs = MutableStateFlow(ConfirmPasswordUiState.Inputs())

    val uiState = inputs.map { inputs ->
        ConfirmPasswordUiState(
            password = inputs.password,
            loading = inputs.loading,
            error = inputs.error,
            success = inputs.success,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ConfirmPasswordUiState())

    fun handleUiEvent(event: ConfirmPasswordUiEvent) {
        when (event) {
            is ConfirmPasswordUiEvent.PasswordValueChange -> {
                inputs.update {
                    it.copy(
                        password = event.value,
                        error = null,
                    )
                }
            }
            is ConfirmPasswordUiEvent.ConfirmPassword -> {
                val password = inputs.value.password

                if (password.isEmpty()) {
                    return
                }

                inputs.update {
                    it.copy(
                        loading = true,
                    )
                }

                viewModelScope.launch {
                    vaultService.unlock(UnlockRequest.Password(password))
                        .onSuccess {
                            inputs.update {
                                it.copy(
                                    error = null,
                                    success = true,
                                    loading = false,
                                )
                            }
                        }
                        .onFailure { error ->
                            inputs.update {
                                it.copy(
                                    error = resources.getString(R.string.unlock_wrong_password),
                                    success = false,
                                    loading = false,
                                )
                            }
                        }
                }

            }
        }
    }
}