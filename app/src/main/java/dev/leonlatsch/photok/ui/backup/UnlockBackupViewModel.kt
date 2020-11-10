/*
 *   Copyright 2020 Leon Latsch
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

package dev.leonlatsch.photok.ui.backup

import android.app.Application
import androidx.databinding.Bindable
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.other.emptyString
import dev.leonlatsch.photok.settings.Config
import dev.leonlatsch.photok.ui.components.ObservableViewModel
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt

/**
 * ViewModel for unlocking a Backup.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class UnlockBackupViewModel @ViewModelInject constructor(
    app: Application,
    private val config: Config
) : ObservableViewModel(app) {

    @Bindable
    var password: String = emptyString()
        set(value) {
            field = value
            notifyChange(BR.password)
        }

    /**
     * Verifies the password and calls [result] with true/false.
     */
    fun verifyPassword(result: (valid: Boolean) -> Unit) = viewModelScope.launch {
        result(BCrypt.checkpw(password, config.securityPassword))
    }
}