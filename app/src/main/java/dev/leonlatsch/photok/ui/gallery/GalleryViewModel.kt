package dev.leonlatsch.photok.ui.gallery

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import dev.leonlatsch.photok.model.repositories.PhotoRepository

class GalleryViewModel @ViewModelInject constructor(
    val photoRepository: PhotoRepository
) : ViewModel() {

    val photos = Pager(
            PagingConfig(
                pageSize = PAGE_SIZE,
                maxSize = MAX_SIZE,
                initialLoadSize = INITIAL_LOAD_SIZE,
            )
    ) {
        photoRepository.getAllPaged()
    }.flow

    companion object {
        private const val PAGE_SIZE = 80
        private const val INITIAL_LOAD_SIZE = 100
        private const val MAX_SIZE = 800
    }
}