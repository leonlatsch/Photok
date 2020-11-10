/*
 *   Copyright 2020 Leon Latsch
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

package dev.leonlatsch.photok.ui.backup

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.DialogRestoreBackupBinding
import dev.leonlatsch.photok.other.hide
import dev.leonlatsch.photok.other.show
import dev.leonlatsch.photok.ui.components.BindableDialogFragment

/**
 * Dialog for loading and validating a backup file.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class RestoreBackupDialogFragment(
    private val uri: Uri
) : BindableDialogFragment<DialogRestoreBackupBinding>(R.layout.dialog_restore_backup) {

    private val viewModel: RestoreBackupViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.addOnPropertyChange(BR.restoreState) {
            when (viewModel.restoreState) {
                RestoreState.INITIALIZE -> {
                    binding.restoreDetails.hide()
                    binding.restoreButton.hide()
                    binding.restoreProgressIndicator.show()
                }
                RestoreState.FILE_VALID -> {
                    binding.restoreDetails.show()
                    binding.restoreButton.show()
                    binding.restoreProgressIndicator.hide()
                }
                RestoreState.FILE_INVALID -> {
                    binding.restoreInvalidWarning.show()
                    binding.restoreCloseButton.show()
                    binding.restoreProgressIndicator.hide()
                }
                RestoreState.FILE_UNLOCKED -> return@addOnPropertyChange
            }
        }

        viewModel.loadAndValidateBackup(uri)
    }

    override fun bind(binding: DialogRestoreBackupBinding) {
        super.bind(binding)
        binding.context = this
        binding.viewModel = viewModel
    }
}