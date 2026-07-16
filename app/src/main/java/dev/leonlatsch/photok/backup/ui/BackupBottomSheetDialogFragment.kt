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

package dev.leonlatsch.photok.backup.ui

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.backup.domain.BackupStrategy
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.uicomponnets.base.processdialogs.BaseProcessBottomSheetDialogFragment


/**
 * Process fragment for backing up photos.
 * See [BackupViewModel]
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class BackupBottomSheetDialogFragment : BaseProcessBottomSheetDialogFragment<Photo>() {

    private val strategy: BackupStrategy.Name by lazy {
        BackupStrategy.Name.valueOf(requireArguments().getString(ARG_STRATEGY)!!)
    }

    override val processingLabelTextResource: Int
        get() = strategy.title
    override val canAbort = true

    override val viewModel: BackupViewModel by viewModels()

    override fun prepareViewModel(items: List<Photo>?) {
        @Suppress("DEPRECATION")
        viewModel.uri = requireArguments().getParcelable(ARG_URI)!!
        viewModel.strategyName = strategy
    }

    companion object {
        private const val ARG_URI = "uri"
        private const val ARG_STRATEGY = "strategy"

        fun newInstance(uri: Uri, strategy: BackupStrategy.Name): BackupBottomSheetDialogFragment =
            BackupBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_URI, uri)
                    putString(ARG_STRATEGY, strategy.name)
                }
            }
    }
}