


package dev.leonlatsch.photok.gallery.albums.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.gallery.albums.ui.compose.AlbumsUiState
import dev.leonlatsch.photok.gallery.albums.ui.navigation.AlbumsNavigationEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val albumsRepositoryImpl: AlbumRepository,
    private val albumUiStateFactory: AlbumUiStateFactory,
) : ViewModel() {

    private val showCreateDialog = MutableStateFlow(false)


    val uiState: StateFlow<AlbumsUiState> = combine(
        albumsRepositoryImpl.observeAllAlbumsWithPhotos(),
        showCreateDialog
    ) { albums, showCreateDialog ->
        albumUiStateFactory.create(albums, showCreateDialog)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), AlbumsUiState.Empty())

    private val navEventChannel = Channel<AlbumsNavigationEvent>()
    val navEvent = navEventChannel.receiveAsFlow()

    fun handleUiEvent(event: AlbumsUiEvent) {
        when (event) {
            AlbumsUiEvent.ShowCreateDialog -> showCreateDialog.value = true
            AlbumsUiEvent.HideCreateDialog -> showCreateDialog.value = false
            is AlbumsUiEvent.OpenAlbum -> navEventChannel.trySend(
                AlbumsNavigationEvent.OpenAlbumDetail(
                    event.uuid
                )
            )
        }
    }
}



package dev.leonlatsch.photok.gallery.albums.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.gallery.albums.ui.compose.AlbumsUiState
import dev.leonlatsch.photok.gallery.albums.ui.navigation.AlbumsNavigationEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val albumsRepositoryImpl: AlbumRepository,
    private val albumUiStateFactory: AlbumUiStateFactory,
) : ViewModel() {

    private val showCreateDialog = MutableStateFlow(false)


    val uiState: StateFlow<AlbumsUiState> = combine(
        albumsRepositoryImpl.observeAllAlbumsWithPhotos(),
        showCreateDialog
    ) { albums, showCreateDialog ->
        albumUiStateFactory.create(albums, showCreateDialog)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), AlbumsUiState.Empty())

    private val navEventChannel = Channel<AlbumsNavigationEvent>()
    val navEvent = navEventChannel.receiveAsFlow()

    fun handleUiEvent(event: AlbumsUiEvent) {
        when (event) {
            AlbumsUiEvent.ShowCreateDialog -> showCreateDialog.value = true
            AlbumsUiEvent.HideCreateDialog -> showCreateDialog.value = false
            is AlbumsUiEvent.OpenAlbum -> navEventChannel.trySend(
                AlbumsNavigationEvent.OpenAlbumDetail(
                    event.uuid
                )
            )
        }
    }
}

