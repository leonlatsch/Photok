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
class ImportBottomSheetDialogFragment(
    uris: List<Uri>,
    private val albumUUID: String? = "",
    private val importSource: ImportSource,
) : BaseProcessBottomSheetDialogFragment<Uri>(
    uris,
    R.string.import_importing,
    true
) {

    override val viewModel: ImportViewModel by viewModels()

    @Inject
    lateinit var inAppReview: InAppReview

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            viewModel.reviewTrigger.collect {
                inAppReview.requestInAppReview(requireActivity())
            }
        }
    }

    override fun prepareViewModel(items: List<Uri>?) {
        viewModel.albumUUID = albumUUID
        viewModel.importSource = importSource
        super.prepareViewModel(items?.reversed()) // Reverse list to keep order in system gallery
    }
}