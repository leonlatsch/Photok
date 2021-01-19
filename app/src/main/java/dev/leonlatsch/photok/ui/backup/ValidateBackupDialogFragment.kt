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

package dev.leonlatsch.photok.ui.backup

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.DialogValidateBackupBinding
import dev.leonlatsch.photok.other.hide
import dev.leonlatsch.photok.other.show
import dev.leonlatsch.photok.ui.components.bindings.BindableDialogFragment
import dev.leonlatsch.photok.ui.process.RestoreBackupBottomSheetDialogFragment

/**
 * Dialog for loading and validating a backup file.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class ValidateBackupDialogFragment(
    private val uri: Uri
) : BindableDialogFragment<DialogValidateBackupBinding>(R.layout.dialog_validate_backup) {

    private val viewModel: ValidateBackupViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.addOnPropertyChange<RestoreState>(BR.restoreState) {
            when (it) {
                RestoreState.INITIALIZE -> {
                    binding.restoreDetails.hide()
                    binding.validateBackupFilename.hide()
                    binding.restoreButton.hide()
                    binding.restoreProgressIndicator.show()
                }
                RestoreState.FILE_VALID -> {
                    binding.restoreDetails.show()
                    binding.validateBackupFilename.show()
                    binding.restoreButton.show()
                    binding.restoreProgressIndicator.hide()
                }
                RestoreState.FILE_INVALID -> {
                    binding.restoreInvalidWarning.show()
                    binding.restoreCloseButton.show()
                    binding.restoreProgressIndicator.hide()
                }
            }
        }

        viewModel.loadAndValidateBackup(uri)
    }

    /**
     * Starts the [UnlockBackupDialogFragment].
     * Called by ui.
     */
    fun onRestoreAndUnlock() {
        UnlockBackupDialogFragment(viewModel.metaData!!.password) { origPassword ->
            dismiss()
            val restoreDialog =
                RestoreBackupBottomSheetDialogFragment(uri, viewModel.metaData!!, origPassword)
            restoreDialog.show(
                requireActivity().supportFragmentManager,
                RestoreBackupBottomSheetDialogFragment::class.qualifiedName
            )
        }.show(
            requireActivity().supportFragmentManager,
            UnlockBackupDialogFragment::class.qualifiedName
        )
    }

    override fun bind(binding: DialogValidateBackupBinding) {
        super.bind(binding)
        binding.context = this
        binding.viewModel = viewModel
    }
}