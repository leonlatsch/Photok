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

package dev.leonlatsch.photok.appstart.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.vaults.domain.VaultRepository
import dev.leonlatsch.photok.vaults.domain.VaultService
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * ViewModel to check the application state.
 * Used by SplashScreen.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class InitialViewModel @Inject constructor(
    private val config: Config,
    private val vaultService: VaultService,
    private val vaultRepository: VaultRepository
) : ViewModel() {

    /**
     * Check the application state.
     */
    fun checkApplicationState(continueStart: (AppStartState) -> Unit) = viewModelScope.launch {

        // First start
        if (config.systemFirstStart) {
            continueStart(AppStartState.FIRST_START)
            return@launch
        }

        if (vaultService.needsMigration()) {
            continueStart(AppStartState.LOCKED)
            return@launch
        }

        if (vaultRepository.hasVaults()) {
            continueStart(AppStartState.LOCKED)
        } else {
            continueStart(AppStartState.SETUP)
        }
    }
}