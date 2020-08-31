package dev.leonlatsch.photok.ui.gallery

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import kotlinx.coroutines.launch

class GalleryViewModel @ViewModelInject constructor(
    private val photoRepository: PhotoRepository
) : ViewModel() {

    val photos = Pager(
            PagingConfig(
                pageSize = 60,
                enablePlaceholders = true,
                maxSize = 500
            )
    ) {
        photoRepository.getAllPaged()
    }.flow

    fun remove(photo: Photo) = viewModelScope.launch {
        photoRepository.delete(photo)
    }
}