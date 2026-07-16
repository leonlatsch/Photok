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

package dev.leonlatsch.photok.gallery.ui.menu

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.uicomponnets.base.processdialogs.BaseProcessBottomSheetDialogFragment

/**
 * Process fragment to export photos.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class ExportBottomSheetDialogFragment : BaseProcessBottomSheetDialogFragment<Photo>() {

    override val processingLabelTextResource = R.string.export_exporting
    override val canAbort = true

    override val viewModel: ExportViewModel by viewModels()

    override fun prepareViewModel(items: List<Photo>?) {
        viewModel.photoUuids = requireArguments().getStringArrayList(ARG_UUIDS)!!
        @Suppress("DEPRECATION")
        viewModel.target = requireArguments().getParcelable(ARG_TARGET)!!
    }

    companion object {
        private const val ARG_UUIDS = "uuids"
        private const val ARG_TARGET = "target"

        fun newInstance(photos: List<Photo>, target: Uri): ExportBottomSheetDialogFragment =
            ExportBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList(ARG_UUIDS, ArrayList(photos.map { it.uuid }))
                    putParcelable(ARG_TARGET, target)
                }
            }
    }
}