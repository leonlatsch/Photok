package dev.leonlatsch.photok.gallery.albums.ui.compose

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.gallery.albums.domain.model.Album
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateAlbumViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
    private val appScope: CoroutineScope,
) : ViewModel() {

    fun createAlbum(name: String) {
        appScope.launch {
            val album = Album(
                name = name,
                modifiedAt = System.currentTimeMillis(),
                files = emptyList(),
            )
            albumRepository.createAlbum(album)
        }
    }
}