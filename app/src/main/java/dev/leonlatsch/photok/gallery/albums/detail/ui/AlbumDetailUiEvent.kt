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

package dev.leonlatsch.photok.gallery.albums.detail.ui

import android.net.Uri
import dev.leonlatsch.photok.gallery.ui.components.ImportChoice
import dev.leonlatsch.photok.gallery.ui.components.PhotoTile

sealed interface AlbumDetailUiEvent {
    data class OpenPhoto(val item: PhotoTile) : AlbumDetailUiEvent
    data class OnDelete(val items: List<String>) : AlbumDetailUiEvent
    data class OnExport(val items: List<String>, val target: Uri?) : AlbumDetailUiEvent
    data class RemoveFromAlbum(val items: List<String>) : AlbumDetailUiEvent
    data object DeleteAlbum : AlbumDetailUiEvent
    data class RenameAlbum(val newName: String) : AlbumDetailUiEvent
    data class OnImportChoice(val choice: ImportChoice) : AlbumDetailUiEvent
}