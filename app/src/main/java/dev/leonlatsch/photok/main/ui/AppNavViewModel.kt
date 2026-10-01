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

package dev.leonlatsch.photok.main.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.encryption.domain.VaultService
import dev.leonlatsch.photok.encryption.migration.LegacyEncryptionMigrator
import dev.leonlatsch.photok.main.ui.navigation.AppRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppNavViewModel @Inject constructor(
    private val vaultService: VaultService,
    private val legacyEncryptionMigrator: LegacyEncryptionMigrator,
) : ViewModel() {

    private val _startRoute = MutableStateFlow<AppRoute?>(null)
    val startRoute = _startRoute.asStateFlow()

    init {
        viewModelScope.launch {
            _startRoute.value = if (vaultService.canUnlock() || legacyEncryptionMigrator.migrationNeeded()) {
                AppRoute.Unlock
            } else {
                AppRoute.Setup
            }
        }
    }
}
