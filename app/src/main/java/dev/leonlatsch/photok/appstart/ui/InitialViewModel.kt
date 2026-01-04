


package dev.leonlatsch.photok.appstart.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.settings.data.Config
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
    private val config: Config
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

        // Unlock or Setup
        val password = config.securityPassword
        val appStartState = if (password == null || password.isEmpty()) {
            AppStartState.SETUP
        } else {
            AppStartState.LOCKED
        }

        continueStart(appStartState)
    }
}

package dev.leonlatsch.photok.appstart.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.settings.data.Config
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
    private val config: Config
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

        // Unlock or Setup
        val password = config.securityPassword
        val appStartState = if (password == null || password.isEmpty()) {
            AppStartState.SETUP
        } else {
            AppStartState.LOCKED
        }

        continueStart(appStartState)
    }
}