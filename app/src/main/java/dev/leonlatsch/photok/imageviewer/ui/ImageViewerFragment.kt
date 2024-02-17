/*
 *   Copyright 2020-2022 Leon Latsch
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

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentImageViewerBinding
import dev.leonlatsch.photok.other.INTENT_PHOTO_UUID
import dev.leonlatsch.photok.other.REQ_PERM_EXPORT
import dev.leonlatsch.photok.other.extensions.*
import dev.leonlatsch.photok.other.statusBarPadding
import dev.leonlatsch.photok.other.systemBarsPadding
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.uicomponnets.Dialogs
import dev.leonlatsch.photok.uicomponnets.bindings.BindableFragment
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

/**
 * Fragment to display photos in full size.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class ImageViewerFragment : BindableFragment<FragmentImageViewerBinding>(R.layout.fragment_image_viewer) {

    private val viewModel: ImageViewerViewModel by viewModels()

    private var systemUiVisible = true

    @Inject
    lateinit var config: Config

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.systemBarsPadding()

        setHasOptionsMenu(true)
        setToolbar(binding.viewPhotoToolbar)
        binding.viewPhotoToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        initializeSystemUI()

        binding.viewPhotoViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.updateDetails(position)
            }
        })

        viewModel.preloadData { uuids ->
            val photoPagerAdapter =
                PhotoPagerAdapter(uuids, viewModel.photoRepository, findNavController(), {
                    binding.viewPhotoViewPager.isUserInputEnabled = !it // On Zoom changed
                }, { // ON CLICK
                    toggleSystemUI()
                })
            binding.viewPhotoViewPager.adapter = photoPagerAdapter

            val photoId = arguments?.getString(INTENT_PHOTO_UUID)
            val startingAt = if (!photoId.isNullOrEmpty()) {
                uuids.indexOf(photoId)
            } else {
                0
            }
            binding.viewPhotoViewPager.setCurrentItem(startingAt, false)
        }
    }

    /**
     * On Detail button clicked.
     * Called by ui.
     */
    fun onDetails() {
        DetailsBottomSheetDialog(viewModel.currentPhoto).show(childFragmentManager)
    }

    /**
     * On delete button clicked.
     * Called by ui.
     */
    fun onDelete() {
        Dialogs.showConfirmDialog(requireContext(), getString(R.string.delete_are_you_sure_this)) { _, _ ->
            viewModel.deletePhoto({ // onSuccess
                requireActivity().onBackPressed()
            }, { // onError
                Dialogs.showLongToast(requireContext(), getString(R.string.common_error))
            })
        }
    }

    /**
     * On export clicked.
     * May request permission WRITE_EXTERNAL_STORAGE.
     * Called by ui.
     */
    @AfterPermissionGranted(REQ_PERM_EXPORT)
    fun onExport() {
        if (EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        ) {
            Dialogs.showConfirmDialog(requireContext(), getString(R.string.export_are_you_sure_this)) { _, _ ->
                viewModel.exportPhoto({ // onSuccess
                    Dialogs.showShortToast(requireContext(), getString(R.string.export_finished))
                }, { // onError
                    Dialogs.showLongToast(requireContext(), getString(R.string.common_error))
                })
            }
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.export_permission_rationale),
                REQ_PERM_EXPORT,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun initializeSystemUI() {

        requireActivity().window.addSystemUIVisibilityListener {
            systemUiVisible = it
            if (it) {
                binding.viewPhotoAppBarLayout.show()
                binding.viewPhotoBottomToolbarLayout.show()
            } else {
                binding.viewPhotoAppBarLayout.hide()
                binding.viewPhotoBottomToolbarLayout.hide()
            }
        }

        if (config.galleryAutoFullscreen) { // Hide system ui if configured
            toggleSystemUI()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_view_photo, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menuViewPhotoInfo -> {
            onDetails()
            true
        }
        else -> false
    }

    private fun toggleSystemUI() {
        if (systemUiVisible) {
            requireActivity().hideSystemUI()
        } else {
            requireActivity().showSystemUI()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().showSystemUI()
    }

    override fun bind(binding: FragmentImageViewerBinding) {
        super.bind(binding)
        binding.context = this
        binding.viewModel = viewModel
    }
}