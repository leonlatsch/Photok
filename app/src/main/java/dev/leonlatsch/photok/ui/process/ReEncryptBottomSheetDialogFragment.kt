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

package dev.leonlatsch.photok.ui.process

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.ui.process.base.BaseProcessBottomSheetDialogFragment

/**
 * Process fragment for re-encrypting photos.
 * Cannot be aborted.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class ReEncryptBottomSheetDialogFragment(
    private val newPassword: String,
) : BaseProcessBottomSheetDialogFragment<Photo>(
    null,
    R.string.change_password_reencrypting,
    false
) {

    override val viewModel: ReEncryptViewModel by viewModels()

    override fun prepareViewModel(items: List<Photo>?) {
        super.prepareViewModel(items)
        viewModel.newPassword = newPassword
    }
}