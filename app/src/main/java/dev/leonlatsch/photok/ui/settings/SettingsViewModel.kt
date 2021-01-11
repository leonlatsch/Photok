/*
 *   Copyright 2020-2021 Leon Latsch
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

package dev.leonlatsch.photok.ui.settings

import android.app.Application
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import dev.leonlatsch.photok.BaseApplication
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.other.empty
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.settings.Config
import dev.leonlatsch.photok.ui.components.bindings.ObservableViewModel
import kotlinx.coroutines.launch

/**
 * ViewModel for Operations started from Settings.
 *
 * @author Leon Latsch
 * @since 1.0.0
 */
class SettingsViewModel @ViewModelInject constructor(
    private val app: Application,
    private val photoRepository: PhotoRepository,
    private val encryptionManager: EncryptionManager,
    private val config: Config
) : ObservableViewModel(app) {

    /**
     * Reset all components and call [BaseApplication.lockApp]
     */
    fun resetComponents() = viewModelScope.launch {
        val uuids = photoRepository.getAllUUIDs()
        for (uuid in uuids) {
            photoRepository.deletePhotoFiles(app, uuid)
        }
        photoRepository.deleteAll()

        config.securityPassword = String.empty
        (app as BaseApplication).lockApp()
    }
}