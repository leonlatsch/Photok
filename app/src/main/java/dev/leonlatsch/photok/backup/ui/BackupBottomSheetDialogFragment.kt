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

package dev.leonlatsch.photok.backup.ui

import android.net.Uri
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
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
class BackupBottomSheetDialogFragment(
    private val uri: Uri,
    private val strategy: BackupStrategy.Name
) : BaseProcessBottomSheetDialogFragment<Photo>(
    itemSource = null,
    processingLabelTextResource = strategy.title,
    canAbort = true,
) {
    override val viewModel: BackupViewModel by viewModels()

    override fun prepareViewModel(items: List<Photo>?) {
        super.prepareViewModel(items)
        viewModel.uri = uri
        viewModel.strategyName = strategy
    }
}