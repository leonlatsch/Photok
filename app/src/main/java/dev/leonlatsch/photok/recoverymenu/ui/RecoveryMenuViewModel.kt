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


package dev.leonlatsch.photok.recoverymenu.ui

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.main.ui.MainActivity
import dev.leonlatsch.photok.settings.ui.hideapp.usecase.ToggleMainComponentUseCase
import javax.inject.Inject

sealed interface RecoveryMenuUiEvent {
    data class OpenPhotok(val activity: Activity?) : RecoveryMenuUiEvent
    data class ResetHideApp(val activity: Activity?) : RecoveryMenuUiEvent
}

@HiltViewModel
class RecoveryMenuViewModel @Inject constructor(
    private val toggleMainComponentUseCase: ToggleMainComponentUseCase,
) : ViewModel() {

    fun handleUiEvent(event: RecoveryMenuUiEvent) {
        when (event) {
            is RecoveryMenuUiEvent.OpenPhotok -> openPhotok(event.activity)
            is RecoveryMenuUiEvent.ResetHideApp -> resetHideApp(event.activity)
        }
    }

    private fun openPhotok(activity: Activity?) {
        activity ?: return

        activity.startActivity(Intent(activity, MainActivity::class.java))
        activity.finish()
    }

    private fun resetHideApp(activity: Activity?) {
        toggleMainComponentUseCase()
        activity?.finish()
    }
}
