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

package dev.leonlatsch.photok.settings.ui.checkpassword

import android.app.Application
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.other.extensions.empty
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.uicomponnets.bindings.ObservableViewModel
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt
import javax.inject.Inject

<<<<<<< HEAD
=======
/**
 * ViewModel for checking the password.
 * Validates password..
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
>>>>>>> 4e67fb4c278072625f2d3298f55a21b329cc8cfc
@HiltViewModel
class CheckPasswordViewModel @Inject constructor(
    app: Application,
    private val config: Config,
) : ObservableViewModel(app) {

    @get:Bindable
    var checkPasswordState: CheckPasswordState = CheckPasswordState.START
        set(value) {
            field = value
            notifyChange(BR.checkPasswordState, value)
        }

    @Bindable
    var oldPassword: String = String.empty
        set(value) {
            field = value
            notifyChange(BR.oldPassword, value)
        }

    /**
     * Checks if the password is valid and updates state. For security concerns.
     * Called by ui.
     */
    fun checkOld() = viewModelScope.launch {
        checkPasswordState = CheckPasswordState.CHECKING_OLD

        val storedPassword = config.securityPassword
        storedPassword ?: return@launch

        checkPasswordState = if (BCrypt.checkpw(oldPassword, storedPassword)) {
            CheckPasswordState.OLD_VALID
        } else {
            CheckPasswordState.OLD_INVALID
        }
    }
}