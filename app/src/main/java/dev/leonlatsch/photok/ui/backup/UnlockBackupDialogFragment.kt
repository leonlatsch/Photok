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

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.DialogBackupUnlockBinding
import dev.leonlatsch.photok.other.extensions.hide
import dev.leonlatsch.photok.other.extensions.show
import dev.leonlatsch.photok.ui.components.bindings.BindableDialogFragment

/**
 * Dialog for unlocking a backup.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class UnlockBackupDialogFragment(
    private val backupPassword: String,
    val onUnlockSuccess: (origPassword: String) -> Unit
) : BindableDialogFragment<DialogBackupUnlockBinding>(R.layout.dialog_backup_unlock) {

    private val viewModel: UnlockBackupViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.addOnPropertyChange<String>(BR.password) {
            binding.unlockBackupWrongPasswordWarning.hide()
        }
    }

    /**
     * Unlocks the safe and calls [onUnlockSuccess] if it matches.
     * Called by ui.
     */
    fun onUnlock() {
        binding.unlockBackupWrongPasswordWarning.hide()
        viewModel.verifyPassword(backupPassword) {
            if (it) {
                dismiss()
                onUnlockSuccess(viewModel.password)
            } else {
                binding.unlockBackupWrongPasswordWarning.show()
            }
        }
    }

    override fun bind(binding: DialogBackupUnlockBinding) {
        super.bind(binding)
        binding.viewModel = viewModel
        binding.context = this
    }
}