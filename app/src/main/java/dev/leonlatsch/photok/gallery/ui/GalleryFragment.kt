/*
 *   Copyright 2020-2021 Leon Latsch
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

import android.Manifest
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.ActionMode
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentGalleryBinding
import dev.leonlatsch.photok.gallery.ui.collections.CollectionAdapter
import dev.leonlatsch.photok.gallery.ui.menu.DeleteBottomSheetDialogFragment
import dev.leonlatsch.photok.gallery.ui.menu.ExportBottomSheetDialogFragment
import dev.leonlatsch.photok.gallery.ui.nav.GalleryNavigator
import dev.leonlatsch.photok.main.ui.MainActivity
import dev.leonlatsch.photok.other.INTENT_PHOTO_ID
import dev.leonlatsch.photok.other.REQ_PERM_EXPORT
import dev.leonlatsch.photok.other.extensions.getBaseApplication
import dev.leonlatsch.photok.other.extensions.requireActivityAs
import dev.leonlatsch.photok.other.extensions.show
import dev.leonlatsch.photok.uicomponnets.Dialogs
import dev.leonlatsch.photok.uicomponnets.bindings.BindableFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

/**
 * Fragment for displaying a gallery.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class GalleryFragment : BindableFragment<FragmentGalleryBinding>(R.layout.fragment_gallery) {

    private val viewModel: GalleryViewModel by viewModels()

    private lateinit var gridAdapter: PhotoAdapter
    private lateinit var collectionAdapter: CollectionAdapter
    private var actionMode: ActionMode? = null

    @Inject
    lateinit var galleryNavigator: GalleryNavigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        setToolbar(binding.galleryToolbar)
        setupCollectionsDrawer()
        setupPhotoGrid()

        gridAdapter.isMultiSelectMode.observe(viewLifecycleOwner, {
            if (it) {
                actionMode = (activity as MainActivity).startSupportActionMode(actionModeCallback)
            } else {
                actionMode?.finish()
            }
        })

        viewModel.navigationEvent.observe(viewLifecycleOwner) {
            galleryNavigator.navigate(it, this)
        }

        viewModel.checkAndShowNewsDialog()
    }

    private fun setupCollectionsDrawer() = with(binding.galleryCollectionsDrawer) {
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        collectionAdapter = CollectionAdapter()
        adapter = collectionAdapter

        collectionAdapter.registerAdapterDataObserver(collectionAdapterDataObserver)

        lifecycleScope.launch {
            viewModel.collections.collectLatest { collectionAdapter.submitData(it) }
        }

        updateCollections()
    }

    private fun setupPhotoGrid() {
        binding.galleryPhotoGrid.layoutManager = GridLayoutManager(requireContext(), getColCount())

        gridAdapter = PhotoAdapter(
            requireContext(),
            viewModel.photoRepository,
            this::openPhoto,
            viewLifecycleOwner
        )

        gridAdapter.registerAdapterDataObserver(gridAdapterDataObserver)
        binding.galleryPhotoGrid.adapter = gridAdapter

        lifecycleScope.launch {
            viewModel.photos.collectLatest { gridAdapter.submitData(it) }
        }

        updatePlaceholder()
    }

    /**
     * Start the deleting process with all selected items.
     * Called by ui.
     */
    fun startDelete() {
        DeleteBottomSheetDialogFragment(gridAdapter.getAllSelected()).show(childFragmentManager)
        gridAdapter.disableSelection()
    }

    /**
     * Starts the exporting process.
     * May request permission WRITE_EXTERNAL_STORAGE.
     * Called by ui.
     */
    @AfterPermissionGranted(REQ_PERM_EXPORT)
    fun startExport() {
        if (EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        ) {
            ExportBottomSheetDialogFragment(gridAdapter.getAllSelected()).show(requireActivity().supportFragmentManager)
            gridAdapter.disableSelection()
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.export_permission_rationale),
                REQ_PERM_EXPORT,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    private val collectionAdapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            updateCollections()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            updateCollections()
        }
    }

    private val gridAdapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) =
            updatePlaceholder()

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) =
            updatePlaceholder()
    }

    private fun updateCollections() {
        viewModel.updateCollections(collectionAdapter.itemCount)
    }

    private fun updatePlaceholder() {
        viewModel.updatePlaceholder(gridAdapter.itemCount)
    }

    private fun getColCount() = when (resources.configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> 4
        Configuration.ORIENTATION_LANDSCAPE -> 8
        else -> 4
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menuMainItemSettings -> {
            findNavController().navigate(R.id.action_galleryFragment_to_settingsFragment)
            true
        }
        R.id.menuMainItemLock -> {
            requireActivity().getBaseApplication().lockApp()
            true
        }
        else -> false
    }

    private fun openPhoto(id: Int) {
        val args = bundleOf(INTENT_PHOTO_ID to id)
        findNavController().navigate(R.id.action_galleryFragment_to_imageViewerFragment, args)
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
                        gridAdapter.selectAll()
                    }
                    true
                }
                R.id.menuMsDelete -> {
                    lifecycleScope.launch {
                        Dialogs.showConfirmDialog(
                            requireContext(),
                            String.format(
                                getString(R.string.delete_are_you_sure),
                                gridAdapter.selectedItems.size
                            )
                        ) { _, _ -> // On positive button clicked
                            startDelete()
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
                                gridAdapter.selectedItems.size
                            )
                        ) { _, _ -> // On positive button clicked
                            startExport()
                        }
                    }
                    true
                }
                else -> false
            }

        override fun onDestroyActionMode(mode: ActionMode?) {
            gridAdapter.disableSelection()
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivityAs(MainActivity::class).onOrientationChanged = {
            setupPhotoGrid()
            setupCollectionsDrawer()
        }
    }

    override fun onPause() {
        super.onPause()
        requireActivityAs(MainActivity::class).onOrientationChanged = {} // Reset
    }

    override fun bind(binding: FragmentGalleryBinding) {
        super.bind(binding)
        binding.context = this
        binding.viewModel = viewModel
    }
}