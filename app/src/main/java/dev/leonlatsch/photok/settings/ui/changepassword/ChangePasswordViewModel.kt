/*
 *   Copyright 2020–2026 Leon Latsch
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

package dev.leonlatsch.photok.settings.ui.changepassword

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.domain.ChangePasswordUseCase
import dev.leonlatsch.photok.encryption.domain.PasswordUtils
import dev.leonlatsch.photok.encryption.domain.VaultService
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionType
import dev.leonlatsch.photok.settings.data.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ChangePasswordUiState(
    val loading: Boolean = false,
    val recoveryPhraseWasUsed: Boolean = false,
    val step: Step = Step.CheckOld,
    val oldPassword: String = "",
    val oldPasswordError: String? = null,
    val newPassword: String = "",
    val newPasswordConfirm: String = "",
    val done: Boolean = false,
) {
    enum class Step {
        CheckOld,
        SetNew,
    }
}


sealed interface ChangePasswordUiEvent {
    data class OldPasswordChanged(val value: String) : ChangePasswordUiEvent
    data object CheckOldPassword : ChangePasswordUiEvent
    data class NewPasswordChanged(val value: String) : ChangePasswordUiEvent
    data class NewPasswordConfirmChanged(val value: String) : ChangePasswordUiEvent
    data object ChangePassword : ChangePasswordUiEvent
}

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val vaultService: VaultService,
    private val config: Config,
    private val resources: Resources,
) : ViewModel() {

    private val recoveryPhraseUsed = config.lastUsedUnlockMethod == VaultProtectionType.RecoveryPhrase

    private val _uiState = MutableStateFlow(
        ChangePasswordUiState(
            recoveryPhraseWasUsed = recoveryPhraseUsed,
            step = if (recoveryPhraseUsed) {
                ChangePasswordUiState.Step.SetNew
            } else {
                ChangePasswordUiState.Step.CheckOld
            },
        )
    )
    val uiState = _uiState.asStateFlow()

    fun handleUiEvent(event: ChangePasswordUiEvent) {
        when (event) {
            is ChangePasswordUiEvent.OldPasswordChanged ->
                _uiState.update { it.copy(oldPassword = event.value, oldPasswordError = null) }
            is ChangePasswordUiEvent.CheckOldPassword -> checkOld()
            is ChangePasswordUiEvent.NewPasswordChanged ->
                _uiState.update { it.copy(newPassword = event.value) }
            is ChangePasswordUiEvent.NewPasswordConfirmChanged ->
                _uiState.update { it.copy(newPasswordConfirm = event.value) }
            is ChangePasswordUiEvent.ChangePassword -> changePassword()
        }
    }

    private fun checkOld() {
        val password = _uiState.value.oldPassword
        _uiState.update { it.copy(loading = true, oldPasswordError = null) }
        viewModelScope.launch {
            vaultService.unlock(UnlockRequest.Password(password))
                .onSuccess {
                    _uiState.update { it.copy(loading = false, step = ChangePasswordUiState.Step.SetNew) }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            loading = false,
                            oldPasswordError = resources.getString(R.string.unlock_wrong_password),
                        )
                    }
                }
        }
    }

    private fun changePassword() {
        val state = _uiState.value
        if (!PasswordUtils.validatePasswords(state.newPassword, state.newPasswordConfirm)) return

        _uiState.update { it.copy(loading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            changePasswordUseCase(state.newPassword)
                .onSuccess { _uiState.update { it.copy(done = true, loading = false) } }
                .onFailure { Timber.e(it) }
        }
    }
}
