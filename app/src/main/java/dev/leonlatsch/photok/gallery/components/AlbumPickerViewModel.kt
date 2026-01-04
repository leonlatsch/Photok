


package dev.leonlatsch.photok.gallery.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.gallery.albums.toUi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AlbumPickerViewModel @Inject constructor(
    private val albumRepository: AlbumRepository
) : ViewModel() {

    val uiState = albumRepository.observeAllAlbumsWithPhotos().map { albums ->
        AlbumPickerUiState(
            albums = albums.map { it.toUi() }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), AlbumPickerUiState())
}


package dev.leonlatsch.photok.gallery.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.gallery.albums.toUi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AlbumPickerViewModel @Inject constructor(
    private val albumRepository: AlbumRepository
) : ViewModel() {

    val uiState = albumRepository.observeAllAlbumsWithPhotos().map { albums ->
        AlbumPickerUiState(
            albums = albums.map { it.toUi() }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), AlbumPickerUiState())
}
