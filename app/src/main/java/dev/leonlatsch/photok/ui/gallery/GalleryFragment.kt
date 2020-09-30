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

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentGalleryBinding
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.other.INTENT_PHOTO_ID
import dev.leonlatsch.photok.ui.MainActivity
import dev.leonlatsch.photok.ui.components.BindableFragment
import dev.leonlatsch.photok.ui.components.Dialogs
import dev.leonlatsch.photok.ui.process.DeleteBottomSheetDialogFragment
import dev.leonlatsch.photok.ui.process.ExportBottomSheetDialogFragment
import dev.leonlatsch.photok.ui.process.ImportBottomSheetDialogFragment
import dev.leonlatsch.photok.ui.viewphoto.ViewPhotoActivity
import kotlinx.android.synthetic.main.fragment_gallery.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Fragment for displaying a gallery.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class GalleryFragment : BindableFragment<FragmentGalleryBinding>(R.layout.fragment_gallery) {

    var placeholderVisibility: MutableLiveData<Int> = MutableLiveData(View.VISIBLE)

    private val viewModel: GalleryViewModel by viewModels()
    private lateinit var adapter: PhotoAdapter
    private var actionMode: ActionMode? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        galleryPhotoGrid.layoutManager = GridLayoutManager(requireContext(), 4)
        viewModel.photos

        adapter = PhotoAdapter(
            requireContext(),
            viewModel.photoRepository,
            this::showFullSize,
            viewLifecycleOwner
        )
        adapter.registerAdapterDataObserver(onAdapterDataObserver)
        galleryPhotoGrid.adapter = adapter
        lifecycleScope.launch {
            viewModel.photos.collectLatest { adapter.submitData(it) }
        }

        adapter.isMultiSelectMode.observe(viewLifecycleOwner, {
            if (it) {
                actionMode = (activity as MainActivity).startActionModeOnToolbar(actionModeCallback)
            } else {
                actionMode?.finish()
            }
        })
    }

    private val onAdapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            togglePlaceholder(adapter.itemCount + itemCount)
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            togglePlaceholder(adapter.itemCount - itemCount)
        }
    }

    private fun togglePlaceholder(itemCount: Int) {
        val visibility = if (itemCount > 0) {
            View.GONE
        } else {
            View.VISIBLE
        }
        placeholderVisibility.postValue(visibility)
    }

    fun startImport() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Select Photos"), REQ_CONTENT_PHOTOS)
    }

    fun startDelete(photos: List<Photo>) {
        val deleteDialog = DeleteBottomSheetDialogFragment(photos)
        deleteDialog.show(
            requireActivity().supportFragmentManager,
            DeleteBottomSheetDialogFragment::class.qualifiedName
        )
    }

    private fun startExport() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(
            Intent.createChooser(intent, "Select Directory"),
            REQ_DOCUMENT_TREE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result from select photos for import
        if (requestCode == REQ_CONTENT_PHOTOS && resultCode == Activity.RESULT_OK) {
            val images = mutableListOf<Uri>()
            if (data != null) {
                if (data.clipData != null) {
                    val count = data.clipData!!.itemCount
                    for (i in 0 until count) {
                        val imageUri = data.clipData!!.getItemAt(i).uri
                        images.add(imageUri)
                    }
                } else if (data.data != null) {
                    val imageUri = data.data!!
                    images.add(imageUri)
                }
            }
            if (images.size > 0) {
                val importDialog = ImportBottomSheetDialogFragment(images)
                importDialog.show(
                    requireActivity().supportFragmentManager,
                    ImportBottomSheetDialogFragment::class.qualifiedName
                )
            }
            // Result from select dir for export
        } else if (requestCode == REQ_DOCUMENT_TREE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {
                val exportDialog = ExportBottomSheetDialogFragment(adapter.getAllSelected(), data.data!!)
                exportDialog.show( // TODO: fix error with adapter getting recreated when activity shows
                    requireActivity().supportFragmentManager,
                    ExportBottomSheetDialogFragment::class.qualifiedName)
                adapter.disableSelection()
            }
        }
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

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean =
            when (item?.itemId) {
                R.id.menuMsAll -> {
                    lifecycleScope.launch {
                        adapter.selectAll()
                    }
                    true
                }
                R.id.menuMsDelete -> {
                    lifecycleScope.launch {
                        Dialogs.showConfirmDialog(
                            requireContext(),
                            String.format(
                                getString(R.string.delete_are_you_sure),
                                adapter.selectedItems.size
                            )
                        ) { _, _ -> // On positive button clicked
                            val selectedItems = adapter.getAllSelected()
                            startDelete(selectedItems)
                        }
                    }
                    true
                }
                R.id.menuMsExport -> {
                    lifecycleScope.launch {
                        Dialogs.showConfirmDialog(
                            requireContext(),
                            String.format(
                                getString(R.string.export_are_you_sure),
                                adapter.selectedItems.size
                            )
                        ) { _, _ -> // On positive button clicked
                            startExport()
                            adapter.disableSelection()
                        }
                    }
                    true
                }
                else -> false
            }

        override fun onDestroyActionMode(mode: ActionMode?) {
            adapter.disableSelection()
        }
    }

    override fun bind(binding: FragmentGalleryBinding) {
        super.bind(binding)
        binding.context = this
    }

    companion object {
        const val REQ_CONTENT_PHOTOS = 0
        const val REQ_DOCUMENT_TREE = 1
    }
}