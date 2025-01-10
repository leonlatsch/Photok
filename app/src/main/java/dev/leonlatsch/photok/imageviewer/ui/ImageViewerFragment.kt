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
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import coil.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentImageViewerBinding
import dev.leonlatsch.photok.imageloading.di.EncryptedImageLoader
import dev.leonlatsch.photok.other.extensions.addSystemUIVisibilityListener
import dev.leonlatsch.photok.other.extensions.hide
import dev.leonlatsch.photok.other.extensions.hideSystemUI
import dev.leonlatsch.photok.other.extensions.show
import dev.leonlatsch.photok.other.extensions.showSystemUI
import dev.leonlatsch.photok.other.systemBarsPadding
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.uicomponnets.Dialogs
import dev.leonlatsch.photok.uicomponnets.bindings.BindableFragment
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

    private val args: ImageViewerFragmentArgs by navArgs()

    @EncryptedImageLoader
    @Inject
    lateinit var encryptedImageLoader: ImageLoader

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.systemBarsPadding()

        setHasOptionsMenu(true)
        setToolbar(binding.viewPhotoToolbar)
        binding.viewPhotoToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        initializeSystemUI()

        binding.viewPhotoViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.updateDetails(position)
            }
        })

        viewModel.preloadData(args.albumUuid) { photos ->
            val photoPagerAdapter =
                PhotoPagerAdapter(
                    photos = photos,
                    encryptedImageLoader = encryptedImageLoader,
                    navController = findNavController(),
                    onClick = {
                        toggleSystemUI()
                    }
                )
            binding.viewPhotoViewPager.adapter = photoPagerAdapter

            val photoUUID = args.photoUuid
            val startingPhoto = photos.find { it.uuid == photoUUID }
            val startingAt = if (!photoUUID.isNullOrEmpty() || startingPhoto != null) {
                photos.indexOf(startingPhoto)
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
                findNavController().navigateUp()
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
    fun onExport() {
        var toastStringAreYouSure = getString(R.string.export_are_you_sure_this)
        var toastStringFinishedExport = getString(R.string.export_finished)
        if (config.deleteExportedFiles) {
            toastStringAreYouSure = getString(R.string.export_and_delete_are_you_sure_this)
            toastStringFinishedExport = getString(R.string.export_and_delete_finished)
        }

        Dialogs.showConfirmDialog(requireContext(), toastStringAreYouSure) { _, _ ->
            viewModel.exportPhoto({ // onSuccess
                if (config.deleteExportedFiles) { // close current photo if deleteExportedFiles is true
                    findNavController().navigateUp()
                }
                Dialogs.showShortToast(requireContext(), toastStringFinishedExport)
            }, { // onError
                Dialogs.showLongToast(requireContext(), getString(R.string.common_error))
            })
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