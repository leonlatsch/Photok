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

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentGalleryBinding
import dev.leonlatsch.photok.other.INTENT_PHOTO_ID
import dev.leonlatsch.photok.ui.MainActivity
import dev.leonlatsch.photok.ui.components.BindableFragment
import dev.leonlatsch.photok.ui.viewphoto.ViewPhotoActivity
import kotlinx.android.synthetic.main.fragment_gallery.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Fragment for displaying a gallery.
 *
 * @since 1.0.0
 */
@AndroidEntryPoint
class GalleryFragment : BindableFragment<FragmentGalleryBinding>(R.layout.fragment_gallery) {

    private val viewModel: GalleryViewModel by viewModels()
    private lateinit var adapter: PhotoAdapter
    private var actionMode: ActionMode? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        galleryPhotoGrid.layoutManager = GridLayoutManager(requireContext(), 4)

        adapter = PhotoAdapter(requireContext(), viewModel.photoRepository, this::showFullSize, viewLifecycleOwner)
        galleryPhotoGrid.adapter = adapter
        lifecycleScope.launch {
            viewModel.photos.collectLatest { adapter.submitData(it) }
        }

        adapter.isMultiSelectMode.observe(viewLifecycleOwner, {
            if (it) {
                actionMode = (activity as MainActivity).startSupportActionMode(actionModeCallback)
            } else {
                actionMode?.finish()
            }
        })
    }

    fun navigateToImport() {
        findNavController().navigate(R.id.action_galleryFragment_to_importFragment)
    }

    private fun showFullSize(id: Int) {
        val intent = Intent(requireActivity(), ViewPhotoActivity::class.java)
        intent.putExtra(INTENT_PHOTO_ID, id)
        startActivity(intent)
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.menu_multi_select, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            // TODO
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            adapter.isMultiSelectMode.postValue(false)
            //TODO: remove checked from ViewHolder
        }
    }

    override fun bind(binding: FragmentGalleryBinding) {
        super.bind(binding)
        binding.context = this
    }
}