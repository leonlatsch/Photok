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

import dev.leonlatsch.photok.model.database.entity.Photo
import javax.inject.Inject

class GalleryUiStateFactory @Inject constructor() {
    fun create(photos: List<Photo>, multiSelectionState: MultiSelectionState, columnCount: Int): GalleryUiState {
        return if (photos.isEmpty()) {
            GalleryUiState.Empty
        } else {
            GalleryUiState.Content(
                selectionMode = false,
                photos = photos.map { PhotoTile(it.fileName, it.type, it.uuid) },
                multiSelectionState = multiSelectionState,
                columnCount = columnCount
            )
        }
    }
}