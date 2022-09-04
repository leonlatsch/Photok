/*
 *   Copyright 2020-2022 Leon Latsch
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

package dev.leonlatsch.photok.recoverymenu

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.other.SingleLiveEvent
import dev.leonlatsch.photok.settings.ui.hideapp.usecase.ToggleMainComponentUseCase
import javax.inject.Inject

@HiltViewModel
class RecoveryMenuViewModel @Inject constructor(
    private val toggleMainComponentUseCase: ToggleMainComponentUseCase
) : ViewModel() {

    val navigationEvent = SingleLiveEvent<RecoveryMenuNavigator.NavigationEvent>()

    fun openPhotok() {
        navigationEvent.value = RecoveryMenuNavigator.NavigationEvent.OpenPhotok
    }

    fun resetHidePhotoSetting() {
        toggleMainComponentUseCase()

        navigationEvent.value = RecoveryMenuNavigator.NavigationEvent.AfterResetHideApp
    }
}