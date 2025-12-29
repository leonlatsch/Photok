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

package dev.leonlatsch.photok.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.settings.domain.GetVaultStatsUseCase
import dev.leonlatsch.photok.settings.domain.VaultStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the About screen.
 *
 * @since 2.0.0
 */
@HiltViewModel
class AboutViewModel @Inject constructor(
    private val getVaultStats: GetVaultStatsUseCase,
) : ViewModel() {

    private val _vaultStats = MutableStateFlow<VaultStats?>(null)
    val vaultStats: StateFlow<VaultStats?> = _vaultStats.asStateFlow()

    init {
        loadVaultStats()
    }

    private fun loadVaultStats() {
        viewModelScope.launch {
            _vaultStats.value = getVaultStats()
        }
    }
}
