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

package dev.leonlatsch.photok.gallery.ui.importing

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.model.repositories.ImportSource
import dev.leonlatsch.photok.review.InAppReview
import dev.leonlatsch.photok.review.ReviewTrigger
import dev.leonlatsch.photok.uicomponnets.base.processdialogs.BaseProcessBottomSheetDialogFragment
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * Process Fragment to import photos.
 * Uses [ImportViewModel] for the process.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class ImportBottomSheetDialogFragment : BaseProcessBottomSheetDialogFragment<Uri>() {

    override val processingLabelTextResource = R.string.import_importing
    override val canAbort = true
    override val itemSource: List<Uri> by lazy {
        @Suppress("DEPRECATION")
        requireArguments().getParcelableArrayList(ARG_URIS)!!
    }

    override val viewModel: ImportViewModel by viewModels()

    @Inject
    lateinit var inAppReview: InAppReview

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            viewModel.reviewTrigger.collect {
                inAppReview.requestInAppReview(requireActivity(), ReviewTrigger.Import)
            }
        }
    }

    override fun prepareViewModel(items: List<Uri>?) {
        viewModel.albumUUID = requireArguments().getString(ARG_ALBUM_UUID)
        viewModel.importSource = ImportSource.valueOf(requireArguments().getString(ARG_IMPORT_SOURCE)!!)
        super.prepareViewModel(items?.reversed()) // Reverse list to keep order in system gallery
    }

    companion object {
        private const val ARG_URIS = "uris"
        private const val ARG_ALBUM_UUID = "album_uuid"
        private const val ARG_IMPORT_SOURCE = "import_source"

        fun newInstance(
            uris: List<Uri>,
            albumUUID: String? = null,
            importSource: ImportSource,
        ): ImportBottomSheetDialogFragment = ImportBottomSheetDialogFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(ARG_URIS, ArrayList(uris))
                putString(ARG_ALBUM_UUID, albumUUID)
                putString(ARG_IMPORT_SOURCE, importSource.name)
            }
        }
    }
}