/*
 *   Copyright 2020-2024 Leon Latsch
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

package dev.leonlatsch.photok.gallery.ui

import dev.leonlatsch.photok.sort.domain.Sort
import dev.leonlatsch.photok.gallery.ui.components.PhotoTile
import dev.leonlatsch.photok.model.database.entity.Photo
import javax.inject.Inject

class GalleryUiStateFactory @Inject constructor() {
    fun create(photos: List<Photo>, showAlbumSelectionDialog: Boolean, sort: Sort): GalleryUiState {
        return if (photos.isEmpty()) {
            GalleryUiState.Empty
        } else {
            GalleryUiState.Content(
                photos = photos.map {
                    PhotoTile(
                        fileName = it.fileName,
                        type = it.type,
                        uuid = it.uuid
                    )
                },
                showAlbumSelectionDialog = showAlbumSelectionDialog,
                sort = sort,
            )
        }
    }
}