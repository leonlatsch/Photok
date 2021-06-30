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

package dev.leonlatsch.photok.ui.gallery

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
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentGalleryBinding
import dev.leonlatsch.photok.other.*
import dev.leonlatsch.photok.ui.MainActivity
import dev.leonlatsch.photok.ui.components.Dialogs
import dev.leonlatsch.photok.ui.components.bindings.BindableFragment
import dev.leonlatsch.photok.ui.news.NewsDialog
import dev.leonlatsch.photok.ui.process.DeleteBottomSheetDialogFragment
import dev.leonlatsch.photok.ui.process.ExportBottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

/**
 * Fragment for displaying a gallery.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class GalleryFragment : BindableFragment<FragmentGalleryBinding>(R.layout.fragment_gallery) {

    private val viewModel: GalleryViewModel by viewModels()

    private lateinit var adapter: PhotoAdapter
    private var actionMode: ActionMode? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        setToolbar(binding.galleryToolbar)
        setupGridView()

        adapter.isMultiSelectMode.observe(viewLifecycleOwner, {
            if (it) {
                actionMode = (activity as MainActivity).startSupportActionMode(actionModeCallback)
            } else {
                actionMode?.finish()
            }
        })

        viewModel.runIfNews {
            NewsDialog().show(requireActivity().supportFragmentManager)
        }
    }

    private fun setupGridView() {
        binding.galleryPhotoGrid.layoutManager = GridLayoutManager(requireContext(), getColCount())

        adapter = PhotoAdapter(
            requireContext(),
            viewModel.photoRepository,
            this::openPhoto,
            viewLifecycleOwner
        )

        adapter.registerAdapterDataObserver(onAdapterDataObserver)
        binding.galleryPhotoGrid.adapter = adapter
        lifecycleScope.launch {
            viewModel.photos.collectLatest { adapter.submitData(it) }
        }
    }

    /**
     * Show [ImportMenuDialog].
     * Called by ui.
     */
    fun showImportMenu() {
        ImportMenuDialog().show(childFragmentManager)
    }

    /**
     * Start the deleting process with all selected items.
     * Called by ui.
     */
    fun startDelete() {
        DeleteBottomSheetDialogFragment(adapter.getAllSelected()).show(requireActivity().supportFragmentManager)
        adapter.disableSelection()
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
            ExportBottomSheetDialogFragment(adapter.getAllSelected()).show(requireActivity().supportFragmentManager)
            adapter.disableSelection()
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.export_permission_rationale),
                REQ_PERM_EXPORT,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    private val onAdapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) =
            viewModel.togglePlaceholder(adapter.itemCount)

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) =
            viewModel.togglePlaceholder(adapter.itemCount)
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
                                adapter.selectedItems.size
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
            adapter.disableSelection()
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivityAs(MainActivity::class).onOrientationChanged = {
            setupGridView()
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