/*
 *   Copyright 2020 Leon Latsch
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

package dev.leonlatsch.photok.ui.gallery

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import dev.leonlatsch.photok.model.repositories.PhotoRepository

/**
 * ViewModel for the Gallery.
 * Holds a Flow for the photos.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class GalleryViewModel @ViewModelInject constructor(
    val photoRepository: PhotoRepository
) : ViewModel() {

    val photos = Pager(
            PagingConfig(
                pageSize = PAGE_SIZE,
                maxSize = MAX_SIZE,
            )
    ) {
        photoRepository.getAllPaged()
    }.flow

    companion object {
        private const val PAGE_SIZE = 100
        private const val MAX_SIZE = 800
    }
}