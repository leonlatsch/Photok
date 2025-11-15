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

package dev.leonlatsch.photok.imageviewer.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.ui.res.stringResource
import androidx.recyclerview.widget.LinearLayoutManager
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.DialogBottomSheetAlbumsCollectionBinding
import dev.leonlatsch.photok.gallery.albums.domain.model.Album
import dev.leonlatsch.photok.other.extensions.hide
import dev.leonlatsch.photok.other.extensions.show
import dev.leonlatsch.photok.uicomponnets.bindings.BindableBottomSheetDialogFragment

sealed class AlbumCollectionUIState {
    object Loading: AlbumCollectionUIState()
    object NoDataFound: AlbumCollectionUIState()
    data class Result(val data: List<Album>): AlbumCollectionUIState()
}

class AlbumsBottomSheetDialog(private val albumState: AlbumCollectionUIState,val onClickAlbum: (Album)-> Unit) : BindableBottomSheetDialogFragment<DialogBottomSheetAlbumsCollectionBinding>(R.layout.dialog_bottom_sheet_albums_collection) {

    override fun bind(binding: DialogBottomSheetAlbumsCollectionBinding) {
        super.bind(binding)

        binding.context = this


        binding.closeAlbumSheet.setOnClickListener {
            this.dismiss()
        }

        when(albumState)
        {
            AlbumCollectionUIState.Loading -> {
                binding.linearProgress.show()
                binding.albumsRecycleView.hide()
                binding.noAlbumFound.hide()
            }
            AlbumCollectionUIState.NoDataFound -> {
                binding.noAlbumFound.show()
                binding.linearProgress.hide()
                binding.albumsRecycleView.hide()
            }
            is AlbumCollectionUIState.Result ->
            {
                binding.albumsRecycleView.show()
                binding.noAlbumFound.hide()
                binding.linearProgress.hide()

                val adapter = AlbumAdapter(albumState.data, onClickAlbum = {
                    onClickAlbum(it)
                    this.dismiss()
                    Toast.makeText(requireContext(), resources.getString(R.string.photo_added_to_album_successfully),Toast.LENGTH_LONG).show()
                })

                binding.albumsRecycleView.layoutManager = LinearLayoutManager(requireContext())
                binding.albumsRecycleView.adapter = adapter
            }
        }
    }
}