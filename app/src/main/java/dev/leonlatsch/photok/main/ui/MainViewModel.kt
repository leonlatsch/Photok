package dev.leonlatsch.photok.main.ui

import android.app.Application
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.ui.importing.SharedUrisStore
import dev.leonlatsch.photok.main.ui.navigation.MainMenuUiState
import dev.leonlatsch.photok.uicomponnets.bindings.ObservableViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * ViewModel for the main activity.
 *
 * @since 1.2.4
 * @author Leon Latsch
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    app: Application,
    private val sharedUrisStore: SharedUrisStore,
) : ObservableViewModel(app) {

    private val _mainMenuUiState = MutableStateFlow(MainMenuUiState(R.id.galleryFragment))
    val mainMenuUiState = _mainMenuUiState.asStateFlow()

    fun addUriToSharedUriStore(uri: Uri) = sharedUrisStore.safeAddUri(uri)

    fun onDestinationChanged(id: Int) {
        _mainMenuUiState.update { it.copy(currentFragmentId = id) }
    }
}