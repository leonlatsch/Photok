/*
 *   Copyright 2020–2026 Leon Latsch
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

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentImageViewerBinding
import dev.leonlatsch.photok.imageloading.compose.LocalEncryptedImageLoader
import dev.leonlatsch.photok.imageloading.di.EncryptedImageLoader
import dev.leonlatsch.photok.imageviewer.ui.compose.ImageViewerScreen
import dev.leonlatsch.photok.other.extensions.addSystemUIVisibilityListener
import dev.leonlatsch.photok.other.extensions.hide
import dev.leonlatsch.photok.other.extensions.hideSystemUI
import dev.leonlatsch.photok.other.extensions.launchAndIgnoreTimer
import dev.leonlatsch.photok.other.extensions.show
import dev.leonlatsch.photok.other.extensions.showSystemUI
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.settings.ui.compose.LocalConfig
import dev.leonlatsch.photok.ui.theme.AppTheme
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
class ImageViewerFragment : Fragment() {

    private val viewModel: ImageViewerViewModel by viewModels()

    private var systemUiVisible = true

    private val pickExportTargetLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            uri ?: return@registerForActivityResult
            onExportTargetPicked(uri)
        }

    private val args: ImageViewerFragmentArgs by navArgs()

    @EncryptedImageLoader
    @Inject
    lateinit var encryptedImageLoader: ImageLoader

    @Inject
    lateinit var config: Config

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    CompositionLocalProvider(
                        LocalEncryptedImageLoader provides encryptedImageLoader,
                        LocalConfig provides config,
                    ) {
                        ImageViewerScreen(args.photoUuid, args.albumUuid)
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeSystemUI()
    }

    /**
     * On Detail button clicked.
     * Called by ui.
     */
    fun onDetailsClicked() {
        DetailsBottomSheetDialog(viewModel.currentPhoto).show(childFragmentManager)
    }

    /**
     * On delete button clicked.
     * Called by ui.
     */
    fun onDeleteClicked() {
        Dialogs.showConfirmDialog(
            requireContext(),
            getString(R.string.delete_are_you_sure_this)
        ) { _, _ ->
            viewModel.deletePhoto(
                onSuccess = { findNavController().navigateUp() },
                onError = { Dialogs.showLongToast(requireContext(), getString(R.string.common_error)) }
            )
        }
    }

    /**
     * On export clicked.
     * May request permission WRITE_EXTERNAL_STORAGE.
     * Called by ui.
     */
    fun onExportClicked() {
        pickExportTargetLauncher.launchAndIgnoreTimer(
            input = null,
            activity = activity,
        )
    }

    private fun onExportTargetPicked(target: Uri) {
        var toastStringAreYouSure = getString(R.string.export_are_you_sure_this)
        var toastStringFinishedExport = getString(R.string.export_finished)
        if (config.deleteExportedFiles) {
            toastStringAreYouSure = getString(R.string.export_and_delete_are_you_sure_this)
            toastStringFinishedExport = getString(R.string.export_and_delete_finished)
        }

        Dialogs.showConfirmDialog(requireContext(), toastStringAreYouSure) { _, _ ->
            viewModel.exportPhoto(
                target = target,
                onSuccess = {
                    if (config.deleteExportedFiles) {
                        findNavController().navigateUp()
                    }
                    Dialogs.showShortToast(requireContext(), toastStringFinishedExport)
                },
                onError = { Dialogs.showLongToast(requireContext(), getString(R.string.common_error)) }
            )
        }
    }

    private fun initializeSystemUI() {
        if (config.galleryAutoFullscreen) { // Hide system ui if configured
            toggleSystemUI()
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.menu_view_photo, menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
//        R.id.menuViewPhotoInfo -> {
//            onDetailsClicked()
//            true
//        }
//
//        else -> false
//    }

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
}