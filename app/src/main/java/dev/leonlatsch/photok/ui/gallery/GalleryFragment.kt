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
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
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
import androidx.recyclerview.widget.SimpleItemAnimator
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentGalleryBinding
import dev.leonlatsch.photok.other.*
import dev.leonlatsch.photok.ui.MainActivity
import dev.leonlatsch.photok.ui.backup.ValidateBackupDialogFragment
import dev.leonlatsch.photok.ui.components.Dialogs
import dev.leonlatsch.photok.ui.components.bindings.BindableFragment
import dev.leonlatsch.photok.ui.process.DeleteBottomSheetDialogFragment
import dev.leonlatsch.photok.ui.process.ExportBottomSheetDialogFragment
import dev.leonlatsch.photok.ui.process.ImportBottomSheetDialogFragment
import dev.leonlatsch.photok.ui.viewphoto.ViewPhotoActivity
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

    // region binding properties

    var placeholderVisibility: MutableLiveData<Int> = MutableLiveData(View.VISIBLE)

    // endregion

    private val viewModel: GalleryViewModel by viewModels()

    private lateinit var adapter: PhotoAdapter
    private var actionMode: ActionMode? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.galleryPhotoGrid.layoutManager = GridLayoutManager(requireContext(), getColCount())
        (binding.galleryPhotoGrid.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false

        adapter = PhotoAdapter(
            requireContext(),
            viewModel.photoRepository,
            this::showFullSize,
            viewLifecycleOwner
        )
        adapter.registerAdapterDataObserver(onAdapterDataObserver)
        binding.galleryPhotoGrid.adapter = adapter
        lifecycleScope.launch {
            viewModel.photos.collectLatest { adapter.submitData(it) }
        }

        adapter.isMultiSelectMode.observe(viewLifecycleOwner, {
            if (it) {
                actionMode = (activity as MainActivity).startActionMode(actionModeCallback)
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

    private fun getColCount() = when (resources.configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> 4
        Configuration.ORIENTATION_LANDSCAPE -> 8
        else -> 4
    }

    private fun togglePlaceholder(itemCount: Int) {
        val visibility = if (itemCount > 0) {
            View.GONE
        } else {
            View.VISIBLE
        }
        placeholderVisibility.postValue(visibility)
    }

    /**
     * Starts the photo import.
     * Starts a chooser for images.
     * May request permission READ_EXTERNAL_STORAGE.
     * Called by ui.
     */
    @AfterPermissionGranted(REQ_PERM_IMPORT)
    fun startImport() {
        if (EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            binding.galleryActionMenu.collapse()
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResultAndIgnoreTimer(
                Intent.createChooser(intent, "Select Photos"),
                REQ_CONTENT_PHOTOS
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.import_permission_rationale),
                REQ_PERM_IMPORT,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    /**
     * Start restoring a backup.
     * Requests permission and shows [ValidateBackupDialogFragment].
     */
    @AfterPermissionGranted(REQ_PERM_RESTORE)
    fun startRestore() {
        if (EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            binding.galleryActionMenu.collapse()
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "application/zip"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResultAndIgnoreTimer(
                Intent.createChooser(intent, "Select Backup"),
                REQ_CONTENT_BACKUP
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.import_permission_rationale),
                REQ_PERM_RESTORE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    /**
     * Start the deleting process with all selected items.
     * Called by ui.
     */
    fun startDelete() {
        val deleteDialog = DeleteBottomSheetDialogFragment(adapter.getAllSelected())
        deleteDialog.show(
            requireActivity().supportFragmentManager,
            DeleteBottomSheetDialogFragment::class.qualifiedName
        )
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
            val exportDialog = ExportBottomSheetDialogFragment(adapter.getAllSelected())
            exportDialog.show(
                requireActivity().supportFragmentManager,
                ExportBottomSheetDialogFragment::class.qualifiedName
            )
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result from select photos for import
        if (requestCode == REQ_CONTENT_PHOTOS && resultCode == Activity.RESULT_OK) {
            val images = mutableListOf<Uri>()
            if (data != null) {
                extractDataFromResult(images, data)
            }
            if (images.size > 0) {
                val importDialog = ImportBottomSheetDialogFragment(images)
                importDialog.show(
                    requireActivity().supportFragmentManager,
                    ImportBottomSheetDialogFragment::class.qualifiedName
                )
            }
        } else if (requestCode == REQ_CONTENT_BACKUP && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                data.data ?: return
                val restoreDialog = ValidateBackupDialogFragment(data.data!!)
                restoreDialog.show(
                    requireActivity().supportFragmentManager,
                    ValidateBackupDialogFragment::class.qualifiedName
                )
            }
        }
    }

    private fun extractDataFromResult(images: MutableList<Uri>, data: Intent): MutableList<Uri> {
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
        return images
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward result to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun bind(binding: FragmentGalleryBinding) {
        super.bind(binding)
        binding.context = this
    }

    companion object {
        const val REQ_CONTENT_PHOTOS = 0
        const val REQ_CONTENT_BACKUP = 1
    }
}