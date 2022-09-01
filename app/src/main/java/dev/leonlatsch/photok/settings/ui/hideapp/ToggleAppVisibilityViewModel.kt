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

package dev.leonlatsch.photok.settings.ui.hideapp

import android.app.Application
import android.view.View
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.other.extensions.empty
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.settings.ui.hideapp.usecase.ToggleMainComponentUseCase
import dev.leonlatsch.photok.uicomponnets.bindings.ObservableViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the HideAppDialog. Holds a state.
 *
 * @since 1.2.0
 * @author Leon Latsch
 */
@HiltViewModel
class ToggleAppVisibilityViewModel @Inject constructor(
    private val app: Application,
    private val config: Config,
    private val toggleMainComponentUseCase: ToggleMainComponentUseCase
) : ObservableViewModel(app) {

    @get:Bindable
    var title: String = String.empty
        set(value) {
            field = value
            notifyChange(BR.title, value)
        }

    @get:Bindable
    var buttonText: String = String.empty
        set(value) {
            field = value
            notifyChange(BR.buttonText, value)
        }

    @get:Bindable
    var buttonEnabled: Boolean = false
        set(value) {
            field = value
            notifyChange(BR.buttonEnabled, value)
        }

    @get:Bindable
    var currentState: String = String.empty
        set(value) {
            field = value
            notifyChange(BR.currentState, value)
        }

    @get:Bindable
    var hintVisibility: Int = View.VISIBLE
        set(value) {
            field = value
            notifyChange(BR.hintVisibility, value)
        }

    var confirmText: String = String.empty

    override fun setup() {
        super.setup()
        if (toggleMainComponentUseCase.isMainComponentDisabled()) {
            title = app.getString(R.string.hide_app_title_show)
            currentState = app.getString(R.string.hide_app_status_hidden)
            hintVisibility = View.GONE
            buttonText = app.getString(R.string.hide_app_title_show)
            buttonEnabled = true
            confirmText = app.getString(R.string.hide_app_confirm_show)
        } else {
            title = app.getString(R.string.hide_app_title_hide)
            currentState = app.getString(R.string.hide_app_status_visible)
            hintVisibility = View.VISIBLE
            confirmText = app.getString(R.string.hide_app_confirm_hide)
            startButtonTextCountDown()
        }

    }

    fun toggleMainComponent() = toggleMainComponentUseCase()

    fun isMainComponentDisabled() = toggleMainComponentUseCase.isMainComponentDisabled()

    /**
     * Constructs a displayable secret launch code.
     */
    fun secretLaunchCode() = app.getString(R.string.settings_security_launch_code_prefix) +
            config.securityDialLaunchCode +
            app.getString(R.string.settings_security_launch_code_suffix)

    private fun startButtonTextCountDown() = viewModelScope.launch {
        var secondsRemaining = 5

        for (a in 1..5) {
            buttonText = secondsRemaining.toString()
            delay(1000)
            secondsRemaining--
        }

        buttonEnabled = true
        buttonText = if (isMainComponentDisabled()) {
            app.getString(R.string.hide_app_title_show)
        } else {
            app.getString(R.string.hide_app_title_hide)
        }
    }
}